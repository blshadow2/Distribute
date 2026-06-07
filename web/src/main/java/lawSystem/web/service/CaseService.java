package lawSystem.web.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lawSystem.jpa.entity.Client;
import lawSystem.jpa.entity.Evidence;
import lawSystem.jpa.entity.LegalCase;
import lawSystem.jpa.entity.Member;
import lawSystem.legalCase.CaseCategory;
import lawSystem.legalCase.CaseStatus;
import lawSystem.web.dto.AiResultResponse;
import lawSystem.web.dto.CaseDto;
import lawSystem.web.dto.CaseForm;
import lawSystem.web.dto.EvidenceDto;
import lawSystem.web.repository.AiResultRepository;
import lawSystem.web.repository.CaseRepository;
import lawSystem.web.repository.EvidenceRepository;
import lawSystem.web.repository.MemberRepository;

/**
 * 사건 등록/조회 서비스.
 * - 등록: 로그인한 의뢰인(Client)의 사건으로 저장 (case_status=INFO_REGISTERED).
 * - 조회: 의뢰인은 본인 사건, 변호사 등은 전체(데모 단순화) — AI 선택 드롭다운에서 사용.
 */
@Service
public class CaseService {

    private final CaseRepository caseRepository;
    private final MemberRepository memberRepository;
    private final EvidenceRepository evidenceRepository;
    private final AiResultRepository aiResultRepository;

    public CaseService(CaseRepository caseRepository,
                       MemberRepository memberRepository,
                       EvidenceRepository evidenceRepository,
                       AiResultRepository aiResultRepository) {
        this.caseRepository = caseRepository;
        this.memberRepository = memberRepository;
        this.evidenceRepository = evidenceRepository;
        this.aiResultRepository = aiResultRepository;
    }

    @Transactional
    public CaseDto create(String clientMemberId, CaseForm form) {
        Member member = memberRepository.findById(clientMemberId)
                .orElseThrow(() -> new IllegalStateException("로그인 회원을 찾을 수 없습니다."));
        if (!(member instanceof Client client)) {
            throw new IllegalStateException("의뢰인만 사건을 등록할 수 있습니다.");
        }

        String caseId = "case-" + UUID.randomUUID().toString().substring(0, 8);
        LegalCase legalCase = new LegalCase(
                caseId,
                client,
                form.getTitle(),
                parseCategory(form.getCategory()),
                form.getCurrentStage(),
                form.getFactDescription(),
                CaseStatus.INFO_REGISTERED);
        caseRepository.save(legalCase);
        return toDto(legalCase);
    }

    @Transactional(readOnly = true)
    public List<CaseDto> listForUser(String memberId, String viewRole) {
        List<LegalCase> cases = "CLIENT".equals(viewRole)
                ? caseRepository.findByClient_MemberIdOrderByCreatedAtDesc(memberId)
                : caseRepository.findAll();
        return cases.stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public CaseDto get(String caseId) {
        return caseRepository.findById(caseId).map(this::toDto).orElse(null);
    }

    /** 변호사 담당(배당된) 사건 목록. */
    @Transactional(readOnly = true)
    public List<CaseDto> listForLawyer(String lawyerMemberId) {
        return caseRepository.findByAssignedLawyer_MemberIdOrderByCreatedAtDesc(lawyerMemberId)
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<EvidenceDto> listEvidence(String caseId) {
        return evidenceRepository.findByLegalCase_CaseId(caseId).stream()
                .map(e -> new EvidenceDto(e.getEvidenceId(), e.getFileName(), e.getFileType(), e.getDescription()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AiResultResponse> listAiResults(String caseId) {
        return aiResultRepository.findByLegalCase_CaseIdOrderByGeneratedAtDesc(caseId).stream()
                .map(r -> new AiResultResponse(
                        r.getAiResultId(),
                        r.getResultType() != null ? r.getResultType().name() : null,
                        r.getSummaryText(),
                        r.getConfidenceScore(),
                        r.getGeneratedAt(),
                        r.isReviewed()))
                .toList();
    }

    @Transactional
    public void addEvidence(String caseId, String fileName, String fileType,
                            String filePath, String description) {
        LegalCase legalCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalStateException("사건을 찾을 수 없습니다: " + caseId));
        Evidence evidence = new Evidence(
                "ev-" + UUID.randomUUID().toString().substring(0, 8),
                fileName, fileType, filePath, description);
        evidence.setLegalCase(legalCase);
        evidenceRepository.save(evidence);
    }

    /** AI가 추출한 키워드를 사건에 저장(최신 추출로 교체). */
    @Transactional
    public void saveKeywords(String caseId, java.util.List<String> keywords) {
        if (caseId == null || keywords == null) {
            return;
        }
        caseRepository.findById(caseId).ifPresent(c -> {
            c.setKeywords(new java.util.ArrayList<>(keywords));
            caseRepository.save(c);
        });
    }

    private CaseDto toDto(LegalCase c) {
        return new CaseDto(
                c.getCaseId(),
                c.getTitle(),
                c.getCategory() != null ? c.getCategory().name() : null,
                c.getCaseStatus() != null ? c.getCaseStatus().name() : null,
                c.getCurrentStage(),
                c.getFactDescription(),
                c.getCreatedAt(),
                new java.util.ArrayList<>(c.getKeywords() != null ? c.getKeywords() : java.util.List.of()));
    }

    private CaseCategory parseCategory(String s) {
        try {
            return CaseCategory.valueOf(s);
        } catch (Exception e) {
            return CaseCategory.ETC;
        }
    }
}
