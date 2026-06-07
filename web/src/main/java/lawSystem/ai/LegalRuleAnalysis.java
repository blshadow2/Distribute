package lawSystem.ai;

import java.util.ArrayList;
import java.util.List;

import lawSystem.db.dao.CaseDocumentDAO;
import lawSystem.legalCase.CaseDocument;
import lawSystem.precedent.LegalRulesDto;
import lawSystem.precedent.PrecedentRagClient;

/**
 * 법리 설명 AI 기능이다. (RAG 결합 — 판례 검색 근거로 법리 분석)
 *
 * - 공식 경로: AIAnalysisFunction.execute → {@link #analyzeLegalIssue(AIAnalysisRequest)}
 * - 직접 호출: {@link #analyzeLegalIssue(String, String)} (문서 본문을 DB 에서 읽어 분석)
 */
public class LegalRuleAnalysis extends AIAnalysisFunction {

    private String targetDocumentId;
    private String legalRuleSource;
    private String explanationLevel;
    private int topK;

    private final PrecedentRagClient aiClient;
    private final CaseDocumentDAO documentDAO;

    public LegalRuleAnalysis() {
        this(new PrecedentRagClient(), new CaseDocumentDAO());
    }

    public LegalRuleAnalysis(PrecedentRagClient aiClient, CaseDocumentDAO documentDAO) {
        super("ai-function-legal-rule-analysis", "법리 분석 기능", "rag-v1", "ACTIVE");
        this.targetDocumentId = null;
        this.legalRuleSource = "판례 RAG + 법령";
        this.explanationLevel = "CLIENT_FRIENDLY";
        this.topK = 5;
        this.aiClient = aiClient;
        this.documentDAO = documentDAO;
    }

    /** 공식 경로(execute) — prompt 를 분석 대상 텍스트로 사용. */
    @Override
    public LegalRuleAnalysisResult analyzeLegalIssue(AIAnalysisRequest request) {
        return buildResult(request.getPrompt(), request.getTargetId(), null,
                request.getAiAnalysisRequestId());
    }

    /** 직접 호출 — 문서 본문을 DB 에서 읽어 법리를 분석한다. */
    public LegalRuleAnalysisResult analyzeLegalIssue(String documentId, String caseId) {
        if (documentId == null || documentId.trim().isEmpty()) {
            return null;
        }
        this.targetDocumentId = documentId;
        String text = loadDocumentText(documentId);
        return buildResult(text, caseId, documentId, "ai-request-direct-" + System.nanoTime());
    }

    private LegalRuleAnalysisResult buildResult(String query, String caseId,
                                                String documentId, String aiRequestId) {
        String issue;
        String law;
        String explanation;
        List<String> statutes;
        double confidence;

        try {
            LegalRulesDto dto = aiClient.analyzeLegalRules(query, caseId, topK);
            issue = dto.getIssueSummary();
            law = dto.getApplicableLaw();
            explanation = dto.getLegalExplanation();
            statutes = dto.getRelatedStatutes();

            if (!dto.getCitedCases().isEmpty()) {
                explanation = explanation + "\n\n[참조 판례 ID] " + String.join(", ", dto.getCitedCases());
            }
            confidence = (explanation == null || explanation.trim().isEmpty()) ? 0.0 : 0.85;
        } catch (Exception e) {
            System.err.println("[RAG] 법리 분석 실패(폴백): " + e.getMessage());
            issue = "";
            law = "";
            explanation = "법리 분석을 생성하지 못했습니다(AI 서비스 오류).";
            statutes = new ArrayList<>();
            confidence = 0.0;
        }

        return new LegalRuleAnalysisResult(
                "ai-result-" + System.nanoTime(),
                aiRequestId,
                caseId,
                "legal-analysis-" + System.nanoTime(),
                documentId,
                issue,
                law,
                explanation,
                statutes,
                confidence);
    }

    private String loadDocumentText(String documentId) {
        try {
            CaseDocument doc = documentDAO.findById(documentId);
            if (doc != null && doc.getContent() != null && !doc.getContent().trim().isEmpty()) {
                return doc.getContent();
            }
        } catch (Exception e) {
            System.err.println("[RAG] 문서 본문 조회 실패: " + e.getMessage());
        }
        return "";
    }

    public String getTargetDocumentId() {
        return targetDocumentId;
    }

    public String getLegalRuleSource() {
        return legalRuleSource;
    }

    public String getExplanationLevel() {
        return explanationLevel;
    }

    public int getTopK() {
        return topK;
    }

    public void setTopK(int topK) {
        this.topK = topK;
    }
}
