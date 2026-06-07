package lawSystem.legalCase;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SimilarPrecedent {
    private String precedentId;
    private String caseId;
    private String precedentTitle;
    private String courtName;
    private String caseNumber;
    private LocalDate decisionDate;
    private String precedentSummary;
    private String legalIssue;
    private String appliedLegalRule;
    private double similarityScore;
    private String sourceUrl;
    private boolean selected;
    private String registeredBy;
    private LocalDateTime registeredAt;

    public SimilarPrecedent(
            String precedentId,
            String caseId,
            String precedentTitle,
            String caseNumber
    ) {
        this.precedentId = precedentId;
        this.caseId = caseId;
        this.precedentTitle = precedentTitle;
        this.caseNumber = caseNumber;
        this.selected = false;
        this.registeredAt = LocalDateTime.now();
    }

    public static SimilarPrecedent registerPrecedent(
            String caseId,
            String precedentTitle,
            String caseNumber
    ) {
        return new SimilarPrecedent(
                "precedent-" + System.currentTimeMillis(),
                caseId,
                precedentTitle,
                caseNumber
        );
    }

    public boolean updatePrecedentSummary(String precedentId, String summary) {
        if (!this.precedentId.equals(precedentId)) {
            return false;
        }

        this.precedentSummary = summary;
        return true;
    }

    public double calculateSimilarity(CaseInfo caseInfo) {
        if (caseInfo == null || caseInfo.getFactDescription() == null) {
            this.similarityScore = 0.0;
            return similarityScore;
        }

        if (precedentSummary != null && caseInfo.getFactDescription().contains(precedentSummary)) {
            this.similarityScore = 1.0;
        } else {
            this.similarityScore = 0.5;
        }

        return similarityScore;
    }

    public boolean selectPrecedent(String precedentId) {
        if (!this.precedentId.equals(precedentId)) {
            return false;
        }

        this.selected = true;
        return true;
    }

    public boolean unselectPrecedent(String precedentId) {
        if (!this.precedentId.equals(precedentId)) {
            return false;
        }

        this.selected = false;
        return true;
    }

    public SimilarPrecedent getPrecedentDetail(String precedentId) {
        if (!this.precedentId.equals(precedentId)) {
            return null;
        }

        return this;
    }

    public String extractLegalIssue(String precedentId) {
        if (!this.precedentId.equals(precedentId)) {
            return null;
        }

        return legalIssue;
    }

    public boolean linkToLegalAnalysis(String legalAnalysisId) {
        return legalAnalysisId != null && !legalAnalysisId.trim().isEmpty();
    }

    public String getPrecedentId() {
        return precedentId;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getPrecedentTitle() {
        return precedentTitle;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    public boolean isSelected() {
        return selected;
    }

    public String getCourtName() {
        return courtName;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public LocalDate getDecisionDate() {
        return decisionDate;
    }

    public String getPrecedentSummary() {
        return precedentSummary;
    }

    public String getLegalIssue() {
        return legalIssue;
    }

    public String getAppliedLegalRule() {
        return appliedLegalRule;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public String getRegisteredBy() {
        return registeredBy;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setCourtName(String courtName) {
        this.courtName = courtName;
    }

    public void setDecisionDate(LocalDate decisionDate) {
        this.decisionDate = decisionDate;
    }

    public void setPrecedentSummary(String precedentSummary) {
        this.precedentSummary = precedentSummary;
    }

    public void setLegalIssue(String legalIssue) {
        this.legalIssue = legalIssue;
    }

    public void setAppliedLegalRule(String appliedLegalRule) {
        this.appliedLegalRule = appliedLegalRule;
    }

    public void setSimilarityScore(double similarityScore) {
        this.similarityScore = similarityScore;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setRegisteredBy(String registeredBy) {
        this.registeredBy = registeredBy;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }
}