package lawSystem.web.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lawSystem.jpa.entity.AssociateLawyer;
import lawSystem.jpa.entity.Lawyer;
import lawSystem.jpa.entity.PartnerLawyer;
import lawSystem.web.dto.LawyerDto;
import lawSystem.web.repository.LawyerRepository;

/**
 * 변호사 검색 서비스. 키워드(이름/소개/전문분야/사무소), 지역, 분야로 필터링한다.
 * 업무량(currentWorkload) 적은 순으로 정렬한다.
 */
@Service
public class LawyerService {

    private final LawyerRepository lawyerRepository;

    public LawyerService(LawyerRepository lawyerRepository) {
        this.lawyerRepository = lawyerRepository;
    }

    @Transactional(readOnly = true)
    public List<LawyerDto> search(String keyword, String region, String specialty) {
        String kw = norm(keyword);
        String rg = norm(region);
        String sp = norm(specialty);

        return lawyerRepository.findAll().stream()
                .filter(l -> matches(l, kw, rg, sp))
                .sorted(Comparator.comparingInt(Lawyer::getCurrentWorkload))
                .map(this::toDto)
                .toList();
    }

    private boolean matches(Lawyer l, String kw, String rg, String sp) {
        // 키워드는 공백/쉼표로 여러 개일 수 있고, 그 중 하나라도 맞으면 통과(사건 키워드 그대로 사용).
        if (!kw.isEmpty() && !keywordHit(l, kw)) {
            return false;
        }
        if (!rg.isEmpty() && !contains(l.getOfficeLocation(), rg)) {
            return false;
        }
        if (!sp.isEmpty() && !anySpecialty(l, sp)) {
            return false;
        }
        return true;
    }

    private boolean keywordHit(Lawyer l, String kw) {
        for (String token : kw.split("[\\s,]+")) {
            if (token.isBlank()) {
                continue;
            }
            if (contains(l.getName(), token) || contains(l.getIntroduction(), token)
                    || contains(l.getOfficeLocation(), token) || anySpecialty(l, token)) {
                return true;
            }
        }
        return false;
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
