package lawSystem.web.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lawSystem.jpa.entity.AssociateLawyer;
import lawSystem.jpa.entity.Lawyer;
import lawSystem.jpa.entity.LegalCase;
import lawSystem.jpa.entity.PartnerLawyer;
import lawSystem.legalCase.CaseStatus;
import lawSystem.web.dto.CaseAssignmentDto;
import lawSystem.web.dto.LawyerDto;
import lawSystem.web.repository.CaseRepository;
import lawSystem.web.repository.LawyerRepository;

/**
 * 사건 배당 서비스 (대표변호사).
 * 수임(RETAINED) 사건을 변호사에게 (재)배정하고, 변호사 업무량(currentWorkload)을 조정한다.
 */
@Service
public class AssignmentService {

    private final CaseRepository caseRepository;
    private final LawyerRepository lawyerRepository;

    public AssignmentService(CaseRepository caseRepository, LawyerRepository lawyerRepository) {
        this.caseRepository = caseRepository;
        this.lawyerRepository = lawyerRepository;
    }

    /** 배당 대상 사건(수임 완료된 RETAINED 사건) 목록. */
    @Transactional(readOnly = true)
    public List<CaseAssignmentDto> listAssignableCases() {
        return caseRepository.findByCaseStatusOrderByCreatedAtDesc(CaseStatus.RETAINED)
                .stream().map(this::toDto).toList();
    }

    /** 배당 가능한 변호사 목록(업무량 적은 순). */
    @Transactional(readOnly = true)
    public List<LawyerDto> listLawyers() {
        return lawyerRepository.findAll().stream()
                .sorted(Comparator.comparingInt(Lawyer::getCurrentWorkload))
                .map(this::toLawyerDto).toList();
    }

    /** 사건을 변호사에게 배당한다. 재배당 시 이전 담당의 업무량을 감소시킨다. */
    @Transactional
    public void assign(String caseId, String lawyerId) {
        LegalCase c = caseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("사건을 찾을 수 없습니다: " + caseId));
        Lawyer target = lawyerRepository.findById(lawyerId)
                .orElseThrow(() -> new IllegalArgumentException("변호사를 선택하세요."));

        Lawyer prev = c.getAssignedLawyer();
        if (prev != null && prev.getMemberId().equals(lawyerId)) {
            return;   // 동일 담당 — 변경 없음
        }
        if (prev != null) {
            prev.setCurrentWorkload(Math.max(0, prev.getCurrentWorkload() - 1));
            lawyerRepository.save(prev);
        }
        target.setCurrentWorkload(target.getCurrentWorkload() + 1);
        lawyerRepository.save(target);

        c.setAssignedLawyer(target);
        c.setCaseStatus(CaseStatus.RETAINED);
        caseRepository.save(c);
    }

    private CaseAssignmentDto toDto(LegalCase c) {
        Lawyer a = c.getAssignedLawyer();
        return new CaseAssignmentDto(
                c.getCaseId(),
                c.getTitle(),
                c.getCategory() != null ? c.getCategory().name() : null,
                c.getCaseStatus() != null ? c.getCaseStatus().name() : null,
                c.getClient() != null ? c.getClient().getName() : "-",
                a != null ? a.getMemberId() : null,
                a != null ? a.getName() : null);
    }

    private LawyerDto toLawyerDto(Lawyer l) {
        String type = (l instanceof PartnerLawyer) ? "대표변호사"
                : (l instanceof AssociateLawyer) ? "소속변호사" : "변호사";
        return new LawyerDto(
                l.getMemberId(), l.getName(), type,
                l.getOfficeLocation(), l.getCurrentWorkload(), l.getIntroduction(),
                new ArrayList<>(l.getSpecialty() != null ? l.getSpecialty() : List.of()));
    }
}
