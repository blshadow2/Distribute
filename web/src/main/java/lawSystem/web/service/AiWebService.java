package lawSystem.web.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import lawSystem.ai.AIAnalysisResult;
import lawSystem.ai.AIAnalysisService;
import lawSystem.ai.AnalysisType;
import lawSystem.ai.CaseAnalysisReport;
import lawSystem.ai.CaseSummary;
import lawSystem.ai.PrecedentAnalysisResult;
import lawSystem.ai.SimilarPrecedentsAnalysis;
import lawSystem.precedent.LegalRulesDto;
import lawSystem.precedent.PrecedentRagClient;
import lawSystem.web.dto.AiResultResponse;
import lawSystem.web.dto.KeywordsResponse;
import lawSystem.web.dto.LegalRulesResponse;
import lawSystem.web.dto.PrecedentDto;
import lawSystem.web.dto.SummaryResponse;
import lawSystem.web.repository.AiResultRepository;

/**
 * AI 기능 웹 서비스.
 *
 * - 쓰기(요약): 기존 도메인 오케스트레이터 {@link AIAnalysisService} 를 그대로 재사용한다.
 *   (요청→호출→저장 + Python RAG 연동까지 처리, JDBC DAO 로 영속화)
 * - 읽기(결과 조회): Spring Data JPA 리포지토리로 ai_analysis_result 를 조회한다.
 */
@Service
public class AiWebService {

    private final AiResultRepository aiResultRepository;
    private final AiPersistenceService aiPersistenceService;

    // 도메인 오케스트레이터/기능 객체는 경량이라 매 호출 생성해도 무방하다.
    private final AIAnalysisService analysisService = new AIAnalysisService();
    private final PrecedentRagClient ragClient = new PrecedentRagClient();

    public AiWebService(AiResultRepository aiResultRepository,
                        AiPersistenceService aiPersistenceService) {
        this.aiResultRepository = aiResultRepository;
        this.aiPersistenceService = aiPersistenceService;
    }

    /** 사건 내용 요약 → ai_analysis_request/result 저장 후 결과 반환. */
    public SummaryResponse summarize(String text, String caseId) {
        AIAnalysisResult result = analysisService.analyze(
                null,                       // requesterId (로그인 연동 시 주입)
                caseId,                     // null 또는 legal_case 에 존재하는 case_id
                AnalysisType.CASE_SUMMARY,
                text,
                new CaseSummary());

        if (result instanceof CaseAnalysisReport report) {
            return new SummaryResponse(
                    report.getAiResultId(),
                    report.getSummary(),
                    report.getMainIssues(),
                    report.getTimeline(),
                    report.getConfidenceScore());
        }
        // 폴백(이론상 도달하지 않음)
        return new SummaryResponse(
                result != null ? result.getAiResultId() : null,
                result != null ? result.getSummaryText() : "요약 생성 실패",
                java.util.List.of(),
                "",
                result != null ? result.getConfidenceScore() : 0.0);
    }

    /** 키워드 추출 (RAG 호출 + 사건이 지정되면 결과를 그 사건으로 저장). */
    public KeywordsResponse extractKeywords(String text, Integer maxKeywords, String caseId) {
        int max = (maxKeywords == null || maxKeywords < 1) ? 5 : maxKeywords;
        KeywordsResponse resp;
        try {
            resp = new KeywordsResponse(ragClient.extractKeywords(text, max));
        } catch (Exception e) {
            resp = new KeywordsResponse(List.of());
        }
        if (caseId != null && !caseId.isBlank() && !resp.getKeywords().isEmpty()) {
            try {
                aiPersistenceService.save(caseId, AnalysisType.KEYWORD_EXTRACTION, text,
                        "키워드: " + String.join(", ", resp.getKeywords()), 0.9);
            } catch (Exception ignore) {
                // 저장 실패는 표시 결과에 영향 주지 않음
            }
        }
        return resp;
    }

    /** 유사 판례 목록 (검색 → DB 본문 조립, 표시용). */
    public List<PrecedentDto> similarPrecedents(String query, String caseId) {
        List<PrecedentDto> out = new ArrayList<>();
        List<PrecedentAnalysisResult> results =
                new SimilarPrecedentsAnalysis().searchSimilarPrecedents(caseId, query);
        for (PrecedentAnalysisResult r : results) {
            out.add(new PrecedentDto(
                    r.getPrecedentId(),
                    r.getPrecedentTitle(),
                    r.getSimilarityScore(),
                    r.getLegalIssue(),
                    r.getPrecedentSummary()));
        }
        return out;
    }

    /** 법리 분석 (RAG 결합). */
    public LegalRulesResponse analyzeLegalRules(String query, String caseId, Integer topK) {
        int k = (topK == null || topK < 1) ? 5 : topK;
        LegalRulesResponse resp;
        try {
            LegalRulesDto d = ragClient.analyzeLegalRules(query, caseId, k);
            resp = new LegalRulesResponse(
                    d.getIssueSummary(), d.getApplicableLaw(), d.getLegalExplanation(),
                    d.getRelatedStatutes(), d.getCitedCases());
        } catch (Exception e) {
            resp = new LegalRulesResponse("", "", "법리 분석 실패(AI 서비스 오류)",
                    List.of(), List.of());
        }
        if (caseId != null && !caseId.isBlank()) {
            try {
                String summary = "[쟁점] " + resp.getIssueSummary()
                        + "\n[적용 법리] " + resp.getApplicableLaw()
                        + "\n[설명] " + resp.getLegalExplanation();
                aiPersistenceService.save(caseId, AnalysisType.LEGAL_RULE_ANALYSIS, query, summary, 0.85);
            } catch (Exception ignore) {
                // 저장 실패는 표시 결과에 영향 주지 않음
            }
        }
        return resp;
    }

    /** 저장된 AI 분석 결과 단건 조회 (Spring Data JPA). */
    @Transactional(readOnly = true)
    public AiResultResponse getResult(String aiResultId) {
        return aiResultRepository.findById(aiResultId)
                .map(e -> new AiResultResponse(
                        e.getAiResultId(),
                        e.getResultType() != null ? e.getResultType().name() : null,
                        e.getSummaryText(),
                        e.getConfidenceScore(),
                        e.getGeneratedAt(),
                        e.isReviewed()))
                .orElse(null);
    }
}
