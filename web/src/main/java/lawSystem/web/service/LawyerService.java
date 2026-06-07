package lawSystem.web.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lawSystem.jpa.entity.AssociateLawyer;
import lawSystem.jpa.entity.Lawyer;
import lawSystem.jpa.entity.PartnerLawyer;
import lawSystem.precedent.PrecedentRagClient;
import lawSystem.web.dto.LawyerDto;
import lawSystem.web.repository.LawyerRepository;

/**
 * 변호사 검색 서비스.
 *
 * - 지역/분야: 명시적 하드 필터.
 * - 키워드: <b>의미 기반(BGE-M3 임베딩) 유사도</b>로 정렬한다. (Python /embed/rerank 재사용)
 *   글자가 정확히 겹치지 않아도(예: "정보보호" ↔ "개인정보") 의미가 가까운 변호사를 상위로 올린다.
 *   RAG 서비스 장애 시에는 기존 부분문자열 매칭 + 업무량 정렬로 graceful degradation.
 */
@Service
public class LawyerService {

    private final LawyerRepository lawyerRepository;
    private final PrecedentRagClient ragClient = new PrecedentRagClient();

    public LawyerService(LawyerRepository lawyerRepository) {
        this.lawyerRepository = lawyerRepository;
    }

    @Transactional(readOnly = true)
    public List<LawyerDto> search(String keyword, String region, String specialty) {
        String kw = norm(keyword);
        String rg = norm(region);
        String sp = norm(specialty);

        // 1) 지역/분야는 하드 필터로 후보를 추린 뒤 DTO 로 미리 변환(이후 지연로딩 없음).
        List<LawyerDto> candidates = lawyerRepository.findAll().stream()
                .filter(l -> rg.isEmpty() || contains(l.getOfficeLocation(), rg))
                .filter(l -> sp.isEmpty() || anySpecialty(l, sp))
                .map(this::toDto)
                .toList();

        // 2) 키워드가 없으면 업무량 적은 순.
        if (kw.isEmpty()) {
            return candidates.stream()
                    .sorted(Comparator.comparingInt(LawyerDto::getCurrentWorkload))
                    .toList();
        }

        // 3) 키워드가 있으면 의미 기반 유사도로 정렬(점수 높은 순, 동점 시 업무량 적은 순).
        Map<String, Double> scores = semanticScores(kw, candidates);
        if (scores != null && !scores.isEmpty()) {
            return candidates.stream()
                    .sorted(Comparator
                            .comparingDouble((LawyerDto d) -> scores.getOrDefault(d.getLawyerId(), -1.0))
                            .reversed()
                            .thenComparingInt(LawyerDto::getCurrentWorkload))
                    .toList();
        }

        // 4) 폴백: RAG 불가 시 기존 부분문자열 매칭 + 업무량.
        return candidates.stream()
                .filter(d -> keywordHit(d, kw))
                .sorted(Comparator.comparingInt(LawyerDto::getCurrentWorkload))
                .toList();
    }

    /** 후보 변호사 프로필을 Python 으로 보내 키워드와의 의미 유사도 점수를 받는다. 실패 시 null. */
    private Map<String, Double> semanticScores(String query, List<LawyerDto> candidates) {
        if (candidates.isEmpty()) {
            return null;
        }
        Map<String, String> profiles = new LinkedHashMap<>();
        for (LawyerDto d : candidates) {
            profiles.put(d.getLawyerId(), profileText(d));
        }
        try {
            return ragClient.rerank(query, profiles);
        } catch (Exception e) {
            return null;   // graceful degradation → 폴백 경로 사용
        }
    }

    /** 임베딩 대상 변호사 프로필 텍스트(전문분야 + 소개). 비면 이름으로 대체. */
    private String profileText(LawyerDto d) {
        StringBuilder sb = new StringBuilder();
        if (d.getSpecialties() != null && !d.getSpecialties().isEmpty()) {
            sb.append("전문분야: ").append(String.join(", ", d.getSpecialties())).append(". ");
        }
        if (d.getIntroduction() != null && !d.getIntroduction().isBlank()) {
            sb.append(d.getIntroduction());
        }
        String text = sb.toString().trim();
        return text.isEmpty() ? d.getName() : text;
    }

    /** 변호사가 자기 사건/판례에서 추출한 키워드를 전문분야(검색 대상)에 누적한다. */
    @Transactional
    public void addKeywords(String lawyerMemberId, List<String> keywords) {
        if (lawyerMemberId == null || keywords == null || keywords.isEmpty()) {
            return;
        }
        lawyerRepository.findById(lawyerMemberId).ifPresent(l -> {
            List<String> specialties = new ArrayList<>(l.getSpecialty() != null ? l.getSpecialty() : List.of());
            for (String k : keywords) {
                if (k != null && !k.isBlank() && !specialties.contains(k.trim())) {
                    specialties.add(k.trim());
                }
            }
            l.setSpecialty(specialties);
            lawyerRepository.save(l);
        });
    }

    // ── 폴백/필터 헬퍼 ────────────────────────────────────────────
    private boolean keywordHit(LawyerDto d, String kw) {
        for (String token : kw.split("[\\s,]+")) {
            if (token.isBlank()) {
                continue;
            }
            if (contains(d.getName(), token) || contains(d.getIntroduction(), token)
                    || contains(d.getOfficeLocation(), token)
                    || (d.getSpecialties() != null
                        && d.getSpecialties().stream().anyMatch(s -> contains(s, token)))) {
                return true;
            }
        }
        return false;
    }

    private boolean anySpecialty(Lawyer l, String term) {
        return l.getSpecialty() != null
                && l.getSpecialty().stream().anyMatch(s -> contains(s, term));
    }

    private boolean contains(String value, String term) {
        return value != null && value.toLowerCase().contains(term.toLowerCase());
    }

    private String norm(String s) {
        return s == null ? "" : s.trim();
    }

    private LawyerDto toDto(Lawyer l) {
        String type = (l instanceof PartnerLawyer) ? "대표변호사"
                : (l instanceof AssociateLawyer) ? "소속변호사" : "변호사";
        return new LawyerDto(
                l.getMemberId(),
                l.getName(),
                type,
                l.getOfficeLocation(),
                l.getCurrentWorkload(),
                l.getIntroduction(),
                new ArrayList<>(l.getSpecialty() != null ? l.getSpecialty() : List.of()));
    }
}
