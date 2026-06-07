package lawSystem.ai;

import java.time.LocalDateTime;

public class AIAnalysisResult {
    private String aiResultId;
    private String aiRequestId;
    private String caseId;
    private AnalysisType resultType;
    private String summaryText;
    private double confidenceScore;
    private LocalDateTime generatedAt;
    private boolean reviewed;
    private String reviewerId;

    public AIAnalysisResult(
            String aiResultId,
            String aiRequestId,
            String caseId,
            AnalysisType resultType,
            String summaryText,
            double confidenceScore
    ) {
        this.aiResultId = aiResultId;
        this.aiRequestId = aiRequestId;
        this.caseId = caseId;
        this.resultType = resultType;
        this.summaryText = summaryText;
        this.confidenceScore = confidenceScore;
        this.generatedAt = LocalDateTime.now();
        this.reviewed = false;
        this.reviewerId = null;
    }

    public boolean saveResult() {
        return aiResultId != null
                && aiRequestId != null
                && caseId != null
                && resultType != null
                && summaryText != null;
    }

    public boolean linkToCase(String caseId) {
        if (caseId == null || caseId.trim().isEmpty()) {
            return false;
        }

        this.caseId = caseId;
        return true;
    }

    public void markReviewed(String reviewerId) {
        if (reviewerId != null && !reviewerId.trim().isEmpty()) {
            this.reviewed = true;
            this.reviewerId = reviewerId;
        }
    }

    public boolean updateResult(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        this.summaryText = content;
        return true;
    }

    protected void updateConfidenceScore(double confidenceScore) {
        if (confidenceScore < 0.0) {
            this.confidenceScore = 0.0;
        } else if (confidenceScore > 1.0) {
            this.confidenceScore = 1.0;
        } else {
            this.confidenceScore = confidenceScore;
        }
    }

    protected void updateResultType(AnalysisType resultType) {
        if (resultType != null) {
            this.resultType = resultType;
        }
    }

    public String getAiResultId() {
        return aiResultId;
    }

    public String getAiRequestId() {
        return aiRequestId;
    }

    public String getCaseId() {
        return caseId;
    }

    public AnalysisType getResultType() {
        return resultType;
    }

    public String getSummaryText() {
        return summaryText;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public boolean isReviewed() {
        return reviewed;
    }

    public String getReviewerId() {
        return reviewerId;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }

    public void setReviewerId(String reviewerId) {
        this.reviewerId = reviewerId;
    }

    @Override
    public String toString() {
        return "AI 결과 ID: " + aiResultId +
                "\nAI 요청 ID: " + aiRequestId +
                "\n사건 ID: " + caseId +
                "\n결과 유형: " + resultType +
                "\n결과 내용: " + summaryText +
                "\n신뢰도: " + confidenceScore +
                "\n생성 일시: " + generatedAt +
                "\n검토 여부: " + reviewed +
                "\n검토자 ID: " + reviewerId;
    }
}