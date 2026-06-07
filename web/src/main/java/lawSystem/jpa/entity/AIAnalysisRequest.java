package lawSystem.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lawSystem.ai.AIRequestStatus;
import lawSystem.ai.AnalysisType;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_analysis_request")
public class AIAnalysisRequest {

    @Id
    @Column(name = "ai_analysis_request_id", length = 64, nullable = false, updatable = false)
    private String aiAnalysisRequestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id")
    private Member requester;

    @Column(name = "target_type", length = 50)
    private String targetType;

    @Column(name = "target_id", length = 64)
    private String targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_type", length = 40, nullable = false)
    private AnalysisType analysisType;

    @Column(name = "prompt", columnDefinition = "TEXT")
    private String prompt;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status", length = 20, nullable = false)
    private AIRequestStatus requestStatus = AIRequestStatus.CREATED;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt = LocalDateTime.now();

    @Column(name = "fail_reason", columnDefinition = "TEXT")
    private String failReason;

    protected AIAnalysisRequest() {}

    public AIAnalysisRequest(String id, Member requester, String targetType, String targetId,
                             AnalysisType analysisType, String prompt) {
        this.aiAnalysisRequestId = id;
        this.requester = requester;
        this.targetType = targetType;
        this.targetId = targetId;
        this.analysisType = analysisType;
        this.prompt = prompt;
    }

    public void markProcessing() { this.requestStatus = AIRequestStatus.PROCESSING; }
    public void markCompleted() { this.requestStatus = AIRequestStatus.COMPLETED; }
    public void markFailed(String reason) { this.requestStatus = AIRequestStatus.FAILED; this.failReason = reason; }

    public String getAiAnalysisRequestId() { return aiAnalysisRequestId; }
    public Member getRequester() { return requester; }
    public String getTargetType() { return targetType; }
    public String getTargetId() { return targetId; }
    public AnalysisType getAnalysisType() { return analysisType; }
    public String getPrompt() { return prompt; }
    public AIRequestStatus getRequestStatus() { return requestStatus; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public String getFailReason() { return failReason; }
}
