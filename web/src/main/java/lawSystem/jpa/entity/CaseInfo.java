package lawSystem.jpa.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lawSystem.legalCase.CaseCategory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "case_info")
public class CaseInfo {

    @Id
    @Column(name = "case_info_id", length = 64, nullable = false, updatable = false)
    private String caseInfoId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", referencedColumnName = "member_id", nullable = false)
    private Client client;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    private LegalCase legalCase;

    @Column(name = "title", length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 30)
    private CaseCategory category;

    @Column(name = "current_stage", length = 100)
    private String currentStage;

    @Column(name = "fact_description", columnDefinition = "TEXT")
    private String factDescription;

    @Column(name = "incident_date")
    private LocalDate incidentDate;

    @Column(name = "region", length = 100)
    private String region;

    @ElementCollection
    @CollectionTable(name = "case_info_keyword", joinColumns = @JoinColumn(name = "case_info_id"))
    @Column(name = "keyword", length = 100)
    private List<String> keywords = new ArrayList<>();

    @Column(name = "temporary_saved", nullable = false)
    private boolean temporarySaved = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    protected CaseInfo() {}

    public CaseInfo(
            String caseInfoId,
            Client client,
            String title,
            CaseCategory category,
            String currentStage,
            String factDescription
    ) {
        this.caseInfoId = caseInfoId;
        this.client = client;
        this.title = title;
        this.category = category;
        this.currentStage = currentStage;
        this.factDescription = factDescription;
    }

    public String getCaseInfoId() { return caseInfoId; }
    public Client getClient() { return client; }
    public LegalCase getLegalCase() { return legalCase; }
    public String getTitle() { return title; }
    public CaseCategory getCategory() { return category; }
    public String getCurrentStage() { return currentStage; }
    public String getFactDescription() { return factDescription; }
    public LocalDate getIncidentDate() { return incidentDate; }
    public String getRegion() { return region; }
    public List<String> getKeywords() { return keywords; }
    public boolean isTemporarySaved() { return temporarySaved; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setLegalCase(LegalCase legalCase) { this.legalCase = legalCase; this.updatedAt = LocalDateTime.now(); }
    public void setIncidentDate(LocalDate incidentDate) { this.incidentDate = incidentDate; this.updatedAt = LocalDateTime.now(); }
    public void setRegion(String region) { this.region = region; this.updatedAt = LocalDateTime.now(); }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; this.updatedAt = LocalDateTime.now(); }
    public void setTemporarySaved(boolean temporarySaved) { this.temporarySaved = temporarySaved; }
}
