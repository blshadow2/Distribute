package lawSystem.ai;

import java.time.LocalDateTime;

public class AIAnalysisRequest {
    private String aiAnalysisRequestId;
    private String requesterId;
    private String targetType;
    private String targetId;
    private AnalysisType analysisType;
    private String prompt;
    private AIRequestStatus requestStatus;
    private LocalDateTime requestedAt;
    private String failReason;

    public AIAnalysisRequest(
            String aiAnalysisRequestId,
            String requesterId,
            String targetType,
            String targetId,
            AnalysisType analysisType,
            String prompt
    ) {
        this.aiAnalysisRequestId = aiAnalysisRequestId;
        this.requesterId = requesterId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.analysisType = analysisType;
        this.prompt = prompt;
        this.requestStatus = AIRequestStatus.CREATED;
        this.requestedAt = LocalDateTime.now();
    }

    public static AIAnalysisRequest createAIRequest(
            String requesterId,
            String targetId,
            AnalysisType analysisType
    ) {
        return new AIAnalysisRequest(
                "ai-request-" + System.currentTimeMillis(),
                requesterId,
                "CASE",
                targetId,
                analysisType,
                ""
        );
    }

    public static AIAnalysisRequest createAIRequest(
            String requesterId,
            String targetType,
            String targetId,
            AnalysisType analysisType,
            String prompt
    ) {
        return new AIAnalysisRequest(
                "ai-request-" + System.currentTimeMillis(),
                requesterId,
                targetType,
                targetId,
                analysisType,
                prompt
        );
    }

    public AIAnalysisResult sendToAIService(AIAnalysisFunction function) {
        if (function == null) {
            markFailed("AI 기능 객체가 없습니다.");
            return null;
        }

        markProcessing();

        AIAnalysisResult result = function.execute(this);

        if (result == null) {
            markFailed("AI 분석 결과 생성에 실패했습니다.");
            return null;
        }

        markCompleted();
        return result;
    }

    public void markProcessing() {
        this.requestStatus = AIRequestStatus.PROCESSING;
    }

    public void markCompleted() {
        this.requestStatus = AIRequestStatus.COMPLETED;
    }

    public void markFailed(String reason) {
        this.requestStatus = AIRequestStatus.FAILED;
        this.failReason = reason;
    }

    public AIAnalysisResult retryRequest(AIAnalysisFunction function) {
        if (requestStatus != AIRequestStatus.FAILED) {
            return null;
        }

        return sendToAIService(function);
    }

    public String getAiAnalysisRequestId() {
        return aiAnalysisRequestId;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public String getTargetType() {
        return targetType;
    }

    public String getTargetId() {
        return targetId;
    }

    public AnalysisType getAnalysisType() {
        return analysisType;
    }

    public String getPrompt() {
        return prompt;
    }

    public AIRequestStatus getRequestStatus() {
        return requestStatus;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setRequestStatus(AIRequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }
}