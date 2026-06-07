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
import lawSystem.ai.AnalysisType;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_analysis_result")
public class AIAnalysisResult {

    @Id
    @Column(name = "ai_result_id", length = 64, nullable = false, updatable = false)
    private String aiResultId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ai_request_id", nullable = false)
    private AIAnalysisRequest request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    private LegalCase legalCase;

    @Enumerated(EnumType.STRING)
    @Column(name = "result_type", length = 40, nullable = false)
    private AnalysisType resultType;

    @Column(name = "summary_text", columnDefinition = "LONGTEXT")
    private String summaryText;

    @Column(name = "confidence_score")
    private double confidenceScore;

    @Column(name = "generated_at", nullable = false, updatable = false)
    private LocalDateTime generatedAt = LocalDateTime.now();

    @Column(name = "reviewed", nullable = false)
    private boolean reviewed = false;

    @Column(name = "reviewer_id", length = 64)
    private String reviewerId;

    protected AIAnalysisResult() {}

    public AIAnalysisResult(String id, AIAnalysisRequest request, LegalCase legalCase,
                            AnalysisType resultType, String summaryText, double confidenceScore) {
        this.aiResultId = id;
        this.request = request;
        this.legalCase = legalCase;
        this.resultType = resultType;
        this.summaryText = summaryText;
        this.confidenceScore = confidenceScore;
    }

    public void markReviewed(String reviewerId) {
        this.reviewed = true;
        this.reviewerId = reviewerId;
    }

    public String getAiResultId() { return aiResultId; }
    public AIAnalysisRequest getRequest() { return request; }
    public LegalCase getLegalCase() { return legalCase; }
    public AnalysisType getResultType() { return resultType; }
    public String getSummaryText() { return summaryText; }
    public double getConfidenceScore() { return confidenceScore; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public boolean isReviewed() { return reviewed; }
    public String getReviewerId() { return reviewerId; }
}
