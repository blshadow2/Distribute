package lawSystem.ai;

import java.util.ArrayList;
import java.util.List;

import lawSystem.precedent.PrecedentRagClient;

/**
 * 사건 키워드 추출 AI 기능이다. (LLM 단순 호출)
 *
 * - 공식 경로: AIAnalysisFunction.execute → {@link #extractKeywords(AIAnalysisRequest)}
 * - 직접 호출: {@link #extractKeywords(String, String, String)}
 */
public class CaseKeywordsExtract extends AIAnalysisFunction {

    private String extractionTarget;
    private int maxKeywordCount;
    private double minWeight;

    private final PrecedentRagClient aiClient;

    public CaseKeywordsExtract() {
        this(new PrecedentRagClient());
    }

    public CaseKeywordsExtract(PrecedentRagClient aiClient) {
        super("ai-function-keyword-extract", "사건 키워드 추출 기능", "llm-v1", "ACTIVE");
        this.extractionTarget = "CASE_INFO";
        this.maxKeywordCount = 5;
        this.minWeight = 0.5;
        this.aiClient = aiClient;
    }

    /** 공식 경로(execute). */
    @Override
    public AIAnalysisResult extractKeywords(AIAnalysisRequest request) {
        return buildResult(request.getTargetId(), request.getPrompt(),
                request.getAiAnalysisRequestId());
    }

    /** 직접 호출 — 제목+내용에서 키워드 추출. */
    public AIAnalysisResult extractKeywords(String caseId, String title, String content) {
        String text = (title == null ? "" : title + "\n") + (content == null ? "" : content);
        return buildResult(caseId, text, "ai-request-direct-" + System.nanoTime());
    }

    private AIAnalysisResult buildResult(String caseId, String text, String aiRequestId) {
        if (caseId == null || caseId.trim().isEmpty()) {
            return null;
        }

        List<String> keywords;
        double confidence;
        try {
            keywords = aiClient.extractKeywords(text, maxKeywordCount);
            confidence = keywords.isEmpty() ? 0.0 : 0.9;
        } catch (Exception e) {
            System.err.println("[LLM] 키워드 추출 실패(폴백): " + e.getMessage());
            keywords = new ArrayList<>();
            confidence = 0.0;
        }

        String resultContent = keywords.isEmpty()
                ? "추출된 키워드가 없습니다."
                : String.join(", ", keywords);

        return new AIAnalysisResult(
                "ai-result-" + System.nanoTime(),
                aiRequestId,
                caseId,
                AnalysisType.KEYWORD_EXTRACTION,
                resultContent,
                confidence);
    }

    public String getExtractionTarget() {
        return extractionTarget;
    }

    public int getMaxKeywordCount() {
        return maxKeywordCount;
    }

    public void setMaxKeywordCount(int maxKeywordCount) {
        this.maxKeywordCount = maxKeywordCount;
    }

    public double getMinWeight() {
        return minWeight;
    }
}
