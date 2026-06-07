package lawSystem.ai;

import java.util.List;

public class DocumentDraft extends AIAnalysisFunction {
    private String documentType;
    private String draftTemplateId;
    private int draftVersion;

    public DocumentDraft() {
        super(
                "ai-function-document-draft",
                "서면 초안 작성 기능",
                "dummy-v1",
                "ACTIVE"
        );

        this.documentType = "소장";
        this.draftTemplateId = "template-basic";
        this.draftVersion = 1;
    }

    public DocumentDraftResult generateDraft(
            String caseId,
            String documentType,
            List<String> legalRules
    ) {
        if (caseId == null || caseId.trim().isEmpty()) {
            return null;
        }

        if (documentType == null || documentType.trim().isEmpty()) {
            return null;
        }

        this.documentType = documentType;

        String content =
                "[" + documentType + "] 문서 초안 더미 결과입니다.\n"
                        + "사용 템플릿: " + draftTemplateId + "\n"
                        + "초안 버전: " + draftVersion + "\n"
                        + "적용 법리: " + legalRules + "\n"
                        + "초안 내용: 원고는 피고의 계약 위반으로 인해 손해를 입었으므로 손해배상을 청구합니다.";

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

    public DocumentDraftResult reviseDraft(
            DocumentDraftResult draftResult,
            String revisedContent
    ) {
        if (draftResult == null) {
            return null;
        }

        if (revisedContent == null || revisedContent.trim().isEmpty()) {
            return null;
        }

        draftVersion++;

        String content =
                "기존 초안 ID [" + draftResult.getDraftResultId() + "]를 수정한 더미 결과입니다.\n"
                        + "사건 ID: " + draftResult.getCaseId() + "\n"
                        + "문서 유형: " + draftResult.getDocumentType() + "\n"
                        + "수정 버전: " + draftVersion + "\n"
                        + "수정 내용: " + revisedContent;

        return new DocumentDraftResult(
                "ai-result-" + System.currentTimeMillis(),
                "ai-request-dummy-" + System.currentTimeMillis(),
                draftResult.getCaseId(),
                "draft-" + System.currentTimeMillis(),
                draftResult.getDocumentType(),
                content,
                "WAITING_REVIEW",
                0.80
        );
    }

    public String saveAsCaseDocument(DocumentDraftResult draftResult) {
        if (draftResult == null) {
            return null;
        }

        return draftResult.saveAsCaseDocument();
    }

    public String getDocumentType() {
        return documentType;
    }

    public String getDraftTemplateId() {
        return draftTemplateId;
    }

    public int getDraftVersion() {
        return draftVersion;
    }
}