package lawSystem.legalCase;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Case {
    private String caseId;
    private String clientId;
    private String assignedLawyerId;
    private String title;
    private CaseCategory category;
    private String currentStage;
    private String factDescription;
    private CaseStatus caseStatus;
    private List<String> keywords;
    private LocalDateTime createdAt;

    // Aggregation 관계
    private CaseInfo caseInfo;
    private List<Evidence> evidences;
    private List<CaseDocument> caseDocuments;
    private List<ProgressionRecord> progressionRecords;
    private List<SimilarPrecedent> similarPrecedents;

    public Case(
            String caseId,
            String clientId,
            String title,
            CaseCategory category,
            String currentStage,
            String factDescription,
            CaseStatus caseStatus
    ) {
        this.caseId = caseId;
        this.clientId = clientId;
        this.assignedLawyerId = null;
        this.title = title;
        this.category = category;
        this.currentStage = currentStage;
        this.factDescription = factDescription;
        this.caseStatus = caseStatus;
        this.keywords = new ArrayList<>();
        this.createdAt = LocalDateTime.now();

        this.evidences = new ArrayList<>();
        this.caseDocuments = new ArrayList<>();
        this.progressionRecords = new ArrayList<>();
        this.similarPrecedents = new ArrayList<>();
    }

    public static Case createCase(CaseInfo caseInfo) {
        if (caseInfo == null) {
            return null;
        }

        Case createdCase = caseInfo.convertToCase();
        createdCase.setCaseInfo(caseInfo);

        return createdCase;
    }

    public boolean updateCaseInfo(CaseInfo caseInfo) {
        if (caseInfo == null) {
            return false;
        }

        Case updatedCase = caseInfo.convertToCase();

        this.caseInfo = caseInfo;
        this.title = updatedCase.title;
        this.category = updatedCase.category;
        this.currentStage = updatedCase.currentStage;
        this.factDescription = updatedCase.factDescription;

        return true;
    }

    public void changeCaseStatus(CaseStatus status) {
        if (status != null) {
            this.caseStatus = status;
        }
    }

    public boolean assignLawyer(String lawyerId) {
        if (lawyerId == null || lawyerId.trim().isEmpty()) {
            return false;
        }

        this.assignedLawyerId = lawyerId;
        return true;
    }

    public boolean addEvidence(Evidence evidence) {
        if (evidence == null) {
            return false;
        }

        this.evidences.add(evidence);
        return true;
    }

    public boolean addCaseDocument(CaseDocument document) {
        if (document == null) {
            return false;
        }

        this.caseDocuments.add(document);
        return true;
    }

    public boolean addProgressRecord(ProgressionRecord record) {
        if (record == null) {
            return false;
        }

        this.progressionRecords.add(record);
        return true;
    }

    public boolean addSimilarPrecedent(SimilarPrecedent precedent) {
        if (precedent == null) {
            return false;
        }

        this.similarPrecedents.add(precedent);
        return true;
    }

    public void saveKeywords(List<String> keywords) {
        if (keywords == null) {
            this.keywords = new ArrayList<>();
            return;
        }

        this.keywords = keywords;
    }

    private void setCaseInfo(CaseInfo caseInfo) {
        this.caseInfo = caseInfo;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getAssignedLawyerId() {
        return assignedLawyerId;
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

    public CaseStatus getCaseStatus() {
        return caseStatus;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public CaseInfo getCaseInfo() {
        return caseInfo;
    }

    public List<Evidence> getEvidences() {
        return evidences;
    }

    public List<CaseDocument> getCaseDocuments() {
        return caseDocuments;
    }

    public List<ProgressionRecord> getProgressionRecords() {
        return progressionRecords;
    }

    public List<SimilarPrecedent> getSimilarPrecedents() {
        return similarPrecedents;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setAssignedLawyerId(String assignedLawyerId) {
        this.assignedLawyerId = assignedLawyerId;
    }

    @Override
    public String toString() {
        return "사건 ID: " + caseId +
                "\n의뢰인 ID: " + clientId +
                "\n담당 변호사 ID: " + assignedLawyerId +
                "\n사건 제목: " + title +
                "\n사건 카테고리: " + category +
                "\n현재 진행 단계: " + currentStage +
                "\n사실관계: " + factDescription +
                "\n사건 상태: " + caseStatus +
                "\n키워드: " + keywords +
                "\n사건 정보 존재 여부: " + (caseInfo != null) +
                "\n증거자료 수: " + evidences.size() +
                "\n사건 문서 수: " + caseDocuments.size() +
                "\n진행 기록 수: " + progressionRecords.size() +
                "\n유사 판례 수: " + similarPrecedents.size() +
                "\n생성일시: " + createdAt;
    }
}