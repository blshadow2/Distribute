package lawSystem.web.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lawSystem.ai.AnalysisType;
import lawSystem.jpa.entity.AIAnalysisRequest;
import lawSystem.jpa.entity.AIAnalysisResult;
import lawSystem.jpa.entity.LegalCase;
import lawSystem.web.repository.AiRequestRepository;
import lawSystem.web.repository.AiResultRepository;
import lawSystem.web.repository.CaseRepository;

/**
 * AI 분석 결과를 JPA(ai_analysis_request/result)로 저장한다.
 * 요청→결과 순서로 저장해 FK(ai_request_id)를 충족한다.
 * caseId 가 실제 사건이면 result.case_id 로 연결되어 사건 상세 이력에 표시된다.
 */
@Service
public class AiPersistenceService {

    private final AiRequestRepository aiRequestRepository;
    private final AiResultRepository aiResultRepository;
    private final CaseRepository caseRepository;

    public AiPersistenceService(AiRequestRepository aiRequestRepository,
                                AiResultRepository aiResultRepository,
                                CaseRepository caseRepository) {
        this.aiRequestRepository = aiRequestRepository;
        this.aiResultRepository = aiResultRepository;
        this.caseRepository = caseRepository;
    }

    @Transactional
    public String save(String caseId, AnalysisType type, String prompt,
                       String summaryText, double confidence) {
        LegalCase legalCase = (caseId != null && !caseId.isBlank())
                ? caseRepository.findById(caseId).orElse(null)
                : null;

        AIAnalysisRequest request = new AIAnalysisRequest(
                "air-" + UUID.randomUUID().toString().substring(0, 8),
                null,                 // requester (로그인 연동 시 주입)
                "CASE",
                caseId,
                type,
                prompt);
        request.markCompleted();
        aiRequestRepository.save(request);

        String resultId = "aire-" + UUID.randomUUID().toString().substring(0, 8);
        AIAnalysisResult result = new AIAnalysisResult(
                resultId, request, legalCase, type, summaryText, confidence);
        aiResultRepository.save(result);
        return resultId;
    }
}
