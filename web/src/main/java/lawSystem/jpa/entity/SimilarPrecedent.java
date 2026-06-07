package lawSystem.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "similar_precedent")
public class SimilarPrecedent {

    @Id
    @Column(name = "precedent_id", length = 64, nullable = false, updatable = false)
    private String precedentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false)
    private LegalCase legalCase;

    @Column(name = "precedent_title", length = 255)
    private String precedentTitle;

    @Column(name = "court_name", length = 150)
    private String courtName;

    @Column(name = "case_number", length = 100)
    private String caseNumber;

    @Column(name = "decision_date")
    private LocalDate decisionDate;

    @Column(name = "precedent_summary", columnDefinition = "TEXT")
    private String precedentSummary;

    @Column(name = "legal_issue", columnDefinition = "TEXT")
    private String legalIssue;

    @Column(name = "applied_legal_rule", columnDefinition = "TEXT")
    private String appliedLegalRule;

    @Column(name = "similarity_score")
    private double similarityScore;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @Column(name = "selected", nullable = false)
    private boolean selected = false;

    @Column(name = "registered_by", length = 64)
    private String registeredBy;

    @Column(name = "registered_at", nullable = false, updatable = false)
    private LocalDateTime registeredAt = LocalDateTime.now();

    protected SimilarPrecedent() {}

    public SimilarPrecedent(String precedentId, String precedentTitle, String caseNumber) {
        this.precedentId = precedentId;
        this.precedentTitle = precedentTitle;
        this.caseNumber = caseNumber;
    }

    public String getPrecedentId() { return precedentId; }
    public LegalCase getLegalCase() { return legalCase; }
    public String getPrecedentTitle() { return precedentTitle; }
    public String getCourtName() { return courtName; }
    public String getCaseNumber() { return caseNumber; }
    public LocalDate getDecisionDate() { return decisionDate; }
    public String getPrecedentSummary() { return precedentSummary; }
    public String getLegalIssue() { return legalIssue; }
    public String getAppliedLegalRule() { return appliedLegalRule; }
    public double getSimilarityScore() { return similarityScore; }
    public String getSourceUrl() { return sourceUrl; }
    public boolean isSelected() { return selected; }
    public String getRegisteredBy() { return registeredBy; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }

    public void setLegalCase(LegalCase legalCase) { this.legalCase = legalCase; }
    public void setCourtName(String courtName) { this.courtName = courtName; }
    public void setDecisionDate(LocalDate decisionDate) { this.decisionDate = decisionDate; }
    public void setPrecedentSummary(String s) { this.precedentSummary = s; }
    public void setLegalIssue(String s) { this.legalIssue = s; }
    public void setAppliedLegalRule(String s) { this.appliedLegalRule = s; }
    public void setSimilarityScore(double v) { this.similarityScore = v; }
    public void setSourceUrl(String s) { this.sourceUrl = s; }
    public void setSelected(boolean b) { this.selected = b; }
    public void setRegisteredBy(String s) { this.registeredBy = s; }
}
