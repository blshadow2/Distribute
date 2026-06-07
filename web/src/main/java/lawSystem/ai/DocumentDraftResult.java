package lawSystem.ai;

public class DocumentDraftResult extends AIAnalysisResult {
    private String draftResultId;
    private String documentType;
    private String draftContent;
    private String reviewStatus;

    public DocumentDraftResult(
            String aiResultId,
            String aiRequestId,
            String caseId,
            String draftResultId,
            String documentType,
            String draftContent,
            String reviewStatus,
            double confidenceScore
    ) {
        super(aiResultId, aiRequestId, caseId, AnalysisType.DOCUMENT_DRAFT, draftContent, confidenceScore);
        this.draftResultId = draftResultId;
        this.documentType = documentType;
        this.draftContent = draftContent;
        this.reviewStatus = reviewStatus;
    }

    public static DocumentDraftResult generateDraft(
            String caseId,
            String documentType
    ) {
        String content = "[" + documentType + "] 문서 초안을 생성한 더미 결과입니다.";

        return new DocumentDraftResult(
                "ai-result-" + System.currentTimeMillis(),
                "ai-request-dummy-" + System.currentTimeMillis(),
                caseId,
                "draft-" + System.currentTimeMillis(),
                documentType,
                content,
                "WAITING_REVIEW",
                0.78
        );
    }

    public boolean updateDraft(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        this.draftContent = content;
        updateResult(content);
        return true;
    }

    public boolean reviewDraft(String reviewerId) {
        if (reviewerId == null || reviewerId.trim().isEmpty()) {
            return false;
        }

        this.reviewStatus = "REVIEWED";
        markReviewed(reviewerId);
        return true;
    }

    public String saveAsCaseDocument() {
        return "case-document-from-" + draftResultId;
    }

    public String getDraftResultId() {
        return draftResultId;
    }

    public String getDocumentType() {
        return documentType;
    }

    public String getDraftContent() {
        return draftContent;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }
}