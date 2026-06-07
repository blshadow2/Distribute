package lawSystem.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lawSystem.jpa.entity.Client;
import lawSystem.jpa.entity.Lawyer;
import lawSystem.jpa.entity.LegalCase;
import lawSystem.jpa.entity.Member;
import lawSystem.jpa.entity.Precedent;
import lawSystem.legalCase.CaseCategory;
import lawSystem.legalCase.CaseStatus;
import lawSystem.web.repository.CaseRepository;
import lawSystem.web.repository.MemberRepository;
import lawSystem.web.repository.PrecedentRepository;

/**
 * 테스트용: 적재된 판례를 각 변호사의 "담당 사건"으로 배치한다.
 * - 판례 한 건 → LegalCase 한 건(의뢰인=시드 client, 담당변호사=각 변호사, 상태 RETAINED).
 * - 변호사별로 PER_LAWYER 건씩 라운드로빈 배정.
 * - 이미 배치된 사건(pcase-<external_case_id>)은 건너뛴다(재실행 안전).
 *
 * DataSeeder(@Order 1, 회원), PrecedentDataLoader(@Order 2, 판례) 다음에 실행된다.
 */
@Component
@Order(3)
public class CaseSeeder implements CommandLineRunner {

    private static final String[] LAWYER_IDS = {"seed-partner", "seed-associate", "seed-assoc2", "seed-assoc3"};
    private static final int PER_LAWYER = 2;

    private final CaseRepository caseRepository;
    private final PrecedentRepository precedentRepository;
    private final MemberRepository memberRepository;

    public CaseSeeder(CaseRepository caseRepository,
                      PrecedentRepository precedentRepository,
                      MemberRepository memberRepository) {
        this.caseRepository = caseRepository;
        this.precedentRepository = precedentRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        try {
            Member cm = memberRepository.findById("seed-client").orElse(null);
            if (!(cm instanceof Client client)) {
                System.err.println("[Seed] 사건 배치: 시드 의뢰인(seed-client) 없음 — 건너뜀");
                return;
            }

            List<Lawyer> lawyers = new ArrayList<>();
            for (String id : LAWYER_IDS) {
                Member m = memberRepository.findById(id).orElse(null);
                if (m instanceof Lawyer l) {
                    lawyers.add(l);
                }
            }
            if (lawyers.isEmpty()) {
                System.err.println("[Seed] 사건 배치: 시드 변호사 없음 — 건너뜀");
                return;
            }

            List<Precedent> precedents = precedentRepository.findAll();
            if (precedents.isEmpty()) {
                System.err.println("[Seed] 사건 배치: precedent 미적재 — 건너뜀");
                return;
            }

            int target = lawyers.size() * PER_LAWYER;
            int created = 0;
            for (int i = 0; i < target && i < precedents.size(); i++) {
                Precedent p = precedents.get(i);
                Lawyer lawyer = lawyers.get(i % lawyers.size());
                String caseId = "pcase-" + p.getExternalCaseId();
                if (caseRepository.existsById(caseId)) {
                    continue;
                }
                LegalCase c = new LegalCase(
                        caseId,
                        client,
                        p.getCaseName() != null ? p.getCaseName() : ("판례 " + p.getExternalCaseId()),
                        mapCategory(p.getCaseType()),
                        "수임",
                        firstNonEmpty(p.getSummary(), p.getIssues(), "판례 기반 시드 사건"),
                        CaseStatus.RETAINED);
                c.setAssignedLawyer(lawyer);
                if (p.getKeywords() != null && !p.getKeywords().isEmpty()) {
                    c.setKeywords(new ArrayList<>(p.getKeywords()));
                }
                caseRepository.save(c);
                created++;
                System.out.println("[Seed] 판례→사건 배치: " + c.getTitle() + " → " + lawyer.getName());
            }
            System.out.println("[Seed] 판례 기반 담당 사건 배치 완료: " + created + "건");
        } catch (Exception e) {
            System.err.println("[Seed] 판례 기반 사건 배치 실패: " + e.getMessage());
        }
    }

    private CaseCategory mapCategory(String caseType) {
        if (caseType != null && caseType.contains("형사")) {
            return CaseCategory.CRIMINAL;
        }
        if (caseType != null && caseType.contains("민사")) {
            return CaseCategory.CIVIL;
        }
        return CaseCategory.CIVIL;
    }

    private String firstNonEmpty(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return "";
    }
}
