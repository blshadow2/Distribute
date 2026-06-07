package lawSystem.legalCase;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CaseInfo {
    private String caseInfoId;
    private String clientId;
    private String caseId;
    private String title;
    private CaseCategory category;
    private String currentStage;
    private String factDescription;
    private LocalDate incidentDate;
    private String region;
    private List<String> keywords;
    private boolean temporarySaved;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CaseInfo(
            String caseInfoId,
            String clientId,
            String title,
            CaseCategory category,
            String currentStage,
            String factDescription
    ) {
        this.caseInfoId = caseInfoId;
        this.clientId = clientId;
        this.title = title;
        this.category = category;
        this.currentStage = currentStage;
        this.factDescription = factDescription;
        this.keywords = new ArrayList<>();
        this.temporarySaved = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static CaseInfo createCaseInfo(
            String clientId,
            String title,
            CaseCategory category,
            String currentStage,
            String factDescription
    ) {
        return new CaseInfo(
                "caseInfo-" + System.currentTimeMillis(),
                clientId,
                title,
                category,
                currentStage,
                factDescription
        );
    }

    public boolean updateCaseInfo(
            String title,
            CaseCategory category,
            String currentStage,
            String factDescription
    ) {
        this.title = title;
        this.category = category;
        this.currentStage = currentStage;
        this.factDescription = factDescription;
        this.updatedAt = LocalDateTime.now();
        return validateRequiredFields();
    }

    public boolean validateRequiredFields() {
        return category != null
                && currentStage != null
                && !currentStage.trim().isEmpty()
                && factDescription != null
                && !factDescription.trim().isEmpty();
    }

    public boolean saveTemporary() {
        this.temporarySaved = true;
        this.updatedAt = LocalDateTime.now();
        return true;
    }

    public boolean saveCaseInfo() {
        if (!validateRequiredFields()) {
            return false;
        }

        this.temporarySaved = false;
        this.updatedAt = LocalDateTime.now();
        return true;
    }

    public void addKeywords(List<String> keywords) {
        if (keywords != null) {
            this.keywords.addAll(keywords);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public boolean updateFactDescription(String factDescription) {
        if (factDescription == null || factDescription.trim().isEmpty()) {
            return false;
        }

        this.factDescription = factDescription;
        this.updatedAt = LocalDateTime.now();
        return true;
    }

    public Case convertToCase() {
        if (!validateRequiredFields()) {
            return null;
        }

        Case createdCase = new Case(
                "case-" + System.currentTimeMillis(),
                clientId,
                title,
                category,
                currentStage,
                factDescription,
                CaseStatus.INFO_REGISTERED
        );

        createdCase.saveKeywords(keywords);
        return createdCase;
    }

    public String getCaseInfoId() {
        return caseInfoId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getTitle() {
        return title;
    }

    public CaseCategory getCategory() {
        return category;
    }

    public String getCurrentStage() {
        return currentStage;
    }

    public String getFactDescription() {
        return factDescription;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public boolean isTemporarySaved() {
        return temporarySaved;
    }

    public LocalDate getIncidentDate() {
        return incidentDate;
    }

    public String getRegion() {
        return region;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setIncidentDate(LocalDate incidentDate) {
        this.incidentDate = incidentDate;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public void setTemporarySaved(boolean temporarySaved) {
        this.temporarySaved = temporarySaved;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}