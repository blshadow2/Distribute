package lawSystem.jpa.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 판례 마스터 카탈로그 엔티티이다 (precedent 테이블).
 *
 * RAG 색인/검색의 단일 진실 공급원이며, 사건(case)과는 독립적이다.
 * keywords 는 precedent_keyword 테이블에 @ElementCollection 으로 매핑한다.
 */
@Entity
@Table(name = "precedent")
public class Precedent {

    @Id
    @Column(name = "precedent_id", length = 64, nullable = false, updatable = false)
    private String precedentId;

    @Column(name = "external_case_id", length = 64, nullable = false, unique = true)
    private String externalCaseId;

    @Column(name = "case_name", length = 255)
    private String caseName;

    @Column(name = "case_number", length = 100)
    private String caseNumber;

    @Column(name = "court_name", length = 150)
    private String courtName;

    @Column(name = "court_type_code", length = 20)
    private String courtTypeCode;

    @Column(name = "decision_date")
    private LocalDate decisionDate;

    @Column(name = "case_type", length = 50)
    private String caseType;

    @Column(name = "judgment_type", length = 50)
    private String judgmentType;

    @Column(name = "issues", columnDefinition = "TEXT")
    private String issues;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "referenced_statutes", columnDefinition = "TEXT")
    private String referencedStatutes;

    @Column(name = "referenced_cases", columnDefinition = "TEXT")
    private String referencedCases;

    @Column(name = "full_text", columnDefinition = "LONGTEXT")
    private String fullText;

    @Column(name = "domain", length = 50)
    private String domain;

    @Column(name = "source", length = 100)
    private String source;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @Column(name = "imported_at", nullable = false, updatable = false)
    private LocalDateTime importedAt = LocalDateTime.now();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "precedent_keyword",
            joinColumns = @JoinColumn(name = "precedent_id")
    )
    @Column(name = "keyword", length = 100)
    private Set<String> keywords = new HashSet<>();

    protected Precedent() {
    }

    public Precedent(String precedentId, String externalCaseId) {
        this.precedentId = precedentId;
        this.externalCaseId = externalCaseId;
    }

    public String getPrecedentId() { return precedentId; }
    public String getExternalCaseId() { return externalCaseId; }
    public String getCaseName() { return caseName; }
    public String getCaseNumber() { return caseNumber; }
    public String getCourtName() { return courtName; }
    public String getCourtTypeCode() { return courtTypeCode; }
    public LocalDate getDecisionDate() { return decisionDate; }
    public String getCaseType() { return caseType; }
    public String getJudgmentType() { return judgmentType; }
    public String getIssues() { return issues; }
    public String getSummary() { return summary; }
    public String getReferencedStatutes() { return referencedStatutes; }
    public String getReferencedCases() { return referencedCases; }
    public String getFullText() { return fullText; }
    public String getDomain() { return domain; }
    public String getSource() { return source; }
    public String getSourceUrl() { return sourceUrl; }
    public LocalDateTime getImportedAt() { return importedAt; }
    public Set<String> getKeywords() { return keywords; }

    public void setCaseName(String v) { this.caseName = v; }
    public void setCaseNumber(String v) { this.caseNumber = v; }
    public void setCourtName(String v) { this.courtName = v; }
    public void setCourtTypeCode(String v) { this.courtTypeCode = v; }
    public void setDecisionDate(LocalDate v) { this.decisionDate = v; }
    public void setCaseType(String v) { this.caseType = v; }
    public void setJudgmentType(String v) { this.judgmentType = v; }
    public void setIssues(String v) { this.issues = v; }
    public void setSummary(String v) { this.summary = v; }
    public void setReferencedStatutes(String v) { this.referencedStatutes = v; }
    public void setReferencedCases(String v) { this.referencedCases = v; }
    public void setFullText(String v) { this.fullText = v; }
    public void setDomain(String v) { this.domain = v; }
    public void setSource(String v) { this.source = v; }
    public void setSourceUrl(String v) { this.sourceUrl = v; }
    public void setKeywords(Set<String> v) { this.keywords = v != null ? v : new HashSet<>(); }
}
