package lawSystem.precedent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 판례 마스터 도메인 객체이다.
 *
 * 외부(국가법령정보센터 등)에서 수집한 판례 원문을 표현하며,
 * precedent 테이블의 단일 진실 공급원(single source of truth)에 대응한다.
 *
 * - {@code precedentId} : 내부 PK (예: "PREC-618473")
 * - {@code externalCaseId} : 원천 데이터의 case_id (예: "618473"). RAG 검색 결과가 이 값으로 역참조한다.
 *
 * 사건별로 등록되는 {@link lawSystem.legalCase.SimilarPrecedent} 와 달리,
 * 이 객체는 사건과 무관한 "판례 카탈로그" 한 건을 의미한다.
 */
public class Precedent {

    private String precedentId;
    private String externalCaseId;
    private String caseName;
    private String caseNumber;
    private String courtName;
    private String courtTypeCode;
    private LocalDate decisionDate;
    private String caseType;
    private String judgmentType;
    private String issues;
    private String summary;
    private String referencedStatutes;
    private String referencedCases;
    private String fullText;
    private String domain;
    private String source;
    private String sourceUrl;
    private LocalDateTime importedAt;
    private List<String> keywords = new ArrayList<>();

    public Precedent() {
    }

    public Precedent(String precedentId, String externalCaseId) {
        this.precedentId = precedentId;
        this.externalCaseId = externalCaseId;
    }

    /**
     * external case id 로부터 결정적(deterministic) 마스터 PK 를 만든다.
     * 같은 원천 데이터를 다시 적재해도 같은 precedent_id 가 나오므로 upsert 가 안전하다.
     */
    public static String buildPrecedentId(String externalCaseId) {
        return "PREC-" + externalCaseId;
    }

    public String getPrecedentId() {
        return precedentId;
    }

    public void setPrecedentId(String precedentId) {
        this.precedentId = precedentId;
    }

    public String getExternalCaseId() {
        return externalCaseId;
    }

    public void setExternalCaseId(String externalCaseId) {
        this.externalCaseId = externalCaseId;
    }

    public String getCaseName() {
        return caseName;
    }

    public void setCaseName(String caseName) {
        this.caseName = caseName;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }

    public String getCourtName() {
        return courtName;
    }

    public void setCourtName(String courtName) {
        this.courtName = courtName;
    }

    public String getCourtTypeCode() {
        return courtTypeCode;
    }

    public void setCourtTypeCode(String courtTypeCode) {
        this.courtTypeCode = courtTypeCode;
    }

    public LocalDate getDecisionDate() {
        return decisionDate;
    }

    public void setDecisionDate(LocalDate decisionDate) {
        this.decisionDate = decisionDate;
    }

    public String getCaseType() {
        return caseType;
    }

    public void setCaseType(String caseType) {
        this.caseType = caseType;
    }

    public String getJudgmentType() {
        return judgmentType;
    }

    public void setJudgmentType(String judgmentType) {
        this.judgmentType = judgmentType;
    }

    public String getIssues() {
        return issues;
    }

    public void setIssues(String issues) {
        this.issues = issues;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getReferencedStatutes() {
        return referencedStatutes;
    }

    public void setReferencedStatutes(String referencedStatutes) {
        this.referencedStatutes = referencedStatutes;
    }

    public String getReferencedCases() {
        return referencedCases;
    }

    public void setReferencedCases(String referencedCases) {
        this.referencedCases = referencedCases;
    }

    public String getFullText() {
        return fullText;
    }

    public void setFullText(String fullText) {
        this.fullText = fullText;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public LocalDateTime getImportedAt() {
        return importedAt;
    }

    public void setImportedAt(LocalDateTime importedAt) {
        this.importedAt = importedAt;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords != null ? keywords : new ArrayList<>();
    }
}
