package lawSystem.jpa.entity;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lawSystem.legalCase.CaseCategory;
import lawSystem.legalCase.CaseStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 사건 엔티티. SQL 예약어 'case' 충돌을 피하기 위해 테이블명은 legal_case.
 * 기존 도메인의 CaseCategory / CaseStatus enum 을 그대로 재사용한다.
 */
@Entity
@Table(name = "legal_case")
public class LegalCase {

    @Id
    @Column(name = "case_id", length = 64, nullable = false, updatable = false)
    private String caseId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", referencedColumnName = "member_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_lawyer_id", referencedColumnName = "member_id")
    private Lawyer assignedLawyer;

    @Column(name = "title", length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 30)
    private CaseCategory category;

    @Column(name = "current_stage", length = 100)
    private String currentStage;

    @Column(name = "fact_description", columnDefinition = "TEXT")
    private String factDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "case_status", length = 50, nullable = false)
    private CaseStatus caseStatus;

    @ElementCollection
    @CollectionTable(name = "case_keyword", joinColumns = @JoinColumn(name = "case_id"))
    @Column(name = "keyword", length = 100)
    private List<String> keywords = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "legalCase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Evidence> evidences = new ArrayList<>();

    @OneToMany(mappedBy = "legalCase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CaseDocument> documents = new ArrayList<>();

    @OneToMany(mappedBy = "legalCase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProgressionRecord> progressionRecords = new ArrayList<>();

    @OneToMany(mappedBy = "legalCase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SimilarPrecedent> similarPrecedents = new ArrayList<>();

    protected LegalCase() {
        // JPA 용
    }

    public LegalCase(
            String caseId,
            Client client,
            String title,
            CaseCategory category,
            String currentStage,
            String factDescription,
            CaseStatus caseStatus
    ) {
        this.caseId = caseId;
        this.client = client;
        this.title = title;
        this.category = category;
        this.currentStage = currentStage;
        this.factDescription = factDescription;
        this.caseStatus = caseStatus;
    }

    // 양방향 매핑 보조 메서드
    public void addEvidence(Evidence evidence) {
        evidences.add(evidence);
        evidence.setLegalCase(this);
    }

    public void addDocument(CaseDocument document) {
        documents.add(document);
        document.setLegalCase(this);
    }

    public void addProgressionRecord(ProgressionRecord record) {
        progressionRecords.add(record);
        record.setLegalCase(this);
    }

    public void addSimilarPrecedent(SimilarPrecedent precedent) {
        similarPrecedents.add(precedent);
        precedent.setLegalCase(this);
    }

    public String getCaseId() { return caseId; }
    public Client getClient() { return client; }
    public Lawyer getAssignedLawyer() { return assignedLawyer; }
    public String getTitle() { return title; }
    public CaseCategory getCategory() { return category; }
    public String getCurrentStage() { return currentStage; }
    public String getFactDescription() { return factDescription; }
    public CaseStatus getCaseStatus() { return caseStatus; }
    public List<String> getKeywords() { return keywords; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<Evidence> getEvidences() { return evidences; }
    public List<CaseDocument> getDocuments() { return documents; }
    public List<ProgressionRecord> getProgressionRecords() { return progressionRecords; }
    public List<SimilarPrecedent> getSimilarPrecedents() { return similarPrecedents; }

    public void setAssignedLawyer(Lawyer assignedLawyer) { this.assignedLawyer = assignedLawyer; }
    public void setTitle(String title) { this.title = title; }
    public void setCategory(CaseCategory category) { this.category = category; }
    public void setCurrentStage(String currentStage) { this.currentStage = currentStage; }
    public void setFactDescription(String factDescription) { this.factDescription = factDescription; }
    public void setCaseStatus(CaseStatus caseStatus) { this.caseStatus = caseStatus; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }
}
