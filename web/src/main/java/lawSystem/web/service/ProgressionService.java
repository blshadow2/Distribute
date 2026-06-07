package lawSystem.web.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lawSystem.jpa.entity.LegalCase;
import lawSystem.jpa.entity.ProgressionRecord;
import lawSystem.web.dto.ProgressionDto;
import lawSystem.web.repository.CaseRepository;
import lawSystem.web.repository.ProgressionRecordRepository;

/**
 * 진행상황 공유(소속변호사 작성) / 열람(의뢰인).
 */
@Service
public class ProgressionService {

    private final ProgressionRecordRepository progressionRepository;
    private final CaseRepository caseRepository;

    public ProgressionService(ProgressionRecordRepository progressionRepository,
                              CaseRepository caseRepository) {
        this.progressionRepository = progressionRepository;
        this.caseRepository = caseRepository;
    }

    @Transactional
    public void addProgress(String caseId, String writerId, String status,
                            String description, String recentAction, String requestedMaterial) {
        LegalCase c = caseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("사건을 찾을 수 없습니다: " + caseId));
        ProgressionRecord r = new ProgressionRecord(
                "prog-" + System.nanoTime(), writerId, status, description, recentAction);
        r.setLegalCase(c);
        if (requestedMaterial != null && !requestedMaterial.isBlank()) {
            r.setRequestedMaterial(requestedMaterial);
        }
        progressionRepository.save(r);
    }

    @Transactional(readOnly = true)
    public List<ProgressionDto> listForClient(String clientMemberId) {
        return progressionRepository.findByLegalCase_Client_MemberIdOrderByProgressIdDesc(clientMemberId)
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ProgressionDto> listForLawyer(String lawyerMemberId) {
        return progressionRepository.findByLegalCase_AssignedLawyer_MemberIdOrderByProgressIdDesc(lawyerMemberId)
                .stream().map(this::toDto).toList();
    }

    private ProgressionDto toDto(ProgressionRecord r) {
        return new ProgressionDto(
                r.getProgressId(),
                r.getLegalCase() != null ? r.getLegalCase().getTitle() : "-",
                r.getProgressStatus(),
                r.getDescription(),
                r.getRecentAction(),
                r.getRequestedMaterial());
    }
}
