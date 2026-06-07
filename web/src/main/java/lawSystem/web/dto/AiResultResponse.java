package lawSystem.web.dto;

import java.time.LocalDateTime;

/** GET /api/ai/results/{id} 응답 (ai_analysis_result 단건). */
public class AiResultResponse {

    private String aiResultId;
    private String resultType;
    private String summaryText;
    private double confidenceScore;
    private LocalDateTime generatedAt;
    private boolean reviewed;

    public AiResultResponse(String aiResultId, String resultType, String summaryText,
                            double confidenceScore, LocalDateTime generatedAt, boolean reviewed) {
        this.aiResultId = aiResultId;
        this.resultType = resultType;
        this.summaryText = summaryText;
        this.confidenceScore = confidenceScore;
        this.generatedAt = generatedAt;
        this.reviewed = reviewed;
    }

    public String getAiResultId() { return aiResultId; }
    public String getResultType() { return resultType; }
    public String getSummaryText() { return summaryText; }
    public double getConfidenceScore() { return confidenceScore; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public boolean isReviewed() { return reviewed; }
}
