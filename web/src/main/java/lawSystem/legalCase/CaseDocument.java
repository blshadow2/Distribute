package lawSystem.legalCase;

public class CaseDocument {
    private String documentId;
    private String caseId;
    private String documentType;
    private String title;
    private String content;
    private String filePath;
    private String createdBy;
    private int version;
    private boolean signed;

    public CaseDocument(
            String documentId,
            String caseId,
            String documentType,
            String title,
            String content,
            String createdBy
    ) {
        this.documentId = documentId;
        this.caseId = caseId;
        this.documentType = documentType;
        this.title = title;
        this.content = content;
        this.createdBy = createdBy;
        this.version = 1;
        this.signed = false;
    }

    public CaseDocument createDocument(String content) {
        this.content = content;
        this.version = 1;
        return this;
    }

    public boolean updateDocument(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        this.content = content;
        this.version++;
        return true;
    }

    public CaseDocument generateDraftByAI(String caseId) {
        this.caseId = caseId;
        this.content = "[AI 초안] 사건 정보를 바탕으로 생성된 문서 초안입니다.";
        this.version++;
        return this;
    }

    public boolean approveDocument() {
        return content != null && !content.trim().isEmpty();
    }

    public boolean signDocument(String memberId) {
        if (memberId == null || memberId.trim().isEmpty()) {
            return false;
        }

        this.signed = true;
        return true;
    }

    public String viewDocument() {
        return content;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getDocumentType() {
        return documentType;
    }

    public boolean isSigned() {
        return signed;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public int getVersion() {
        return version;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setSigned(boolean signed) {
        this.signed = signed;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}