package lawSystem.member;

import lawSystem.legalCase.Case;
import lawSystem.legalCase.CaseDocument;
import lawSystem.legalCase.ProgressionRecord;

import java.util.ArrayList;
import java.util.List;

public class AssociateLawyer extends Lawyer {
    private String associateLawyerId;
    private List<Case> assignedCase;

    public AssociateLawyer(
            String memberId,
            String lawyerId,
            String associateLawyerId,
            String name,
            String email,
            String password,
            String phoneNumber,
            String licenseNumber,
            String officeLocation,
            List<String> specialty,
            String introduction
    ) {
        super(
                memberId,
                lawyerId,
                name,
                email,
                password,
                phoneNumber,
                licenseNumber,
                officeLocation,
                specialty,
                introduction
        );

        this.associateLawyerId = associateLawyerId;
        this.assignedCase = new ArrayList<>();
    }

    public String organizeCaseMaterial(String caseId) {
        if (caseId == null || caseId.trim().isEmpty()) {
            return "사건 ID가 없어 사건 자료를 정리할 수 없습니다.";
        }

        Case targetCase = findAssignedCase(caseId);

        if (targetCase == null) {
            return "담당 사건이 아니므로 사건 자료를 정리할 수 없습니다.";
        }

        /*
         * 문서의 원래 타입은 AIAnalysisResult지만,
         * 아직 AIAnalysisResult 클래스를 작성하지 않았으므로
         * 현재는 텍스트 기반 테스트를 위해 String으로 반환한다.
         */
        return "사건 ID [" + caseId + "]의 사건 정보와 증거 자료를 정리했습니다.";
    }

    public CaseDocument writeCaseDocument(String caseId, String documentType) {
        if (caseId == null || caseId.trim().isEmpty()) {
            return null;
        }

        if (documentType == null || documentType.trim().isEmpty()) {
            return null;
        }

        Case targetCase = findAssignedCase(caseId);

        if (targetCase == null) {
            System.out.println("담당 사건이 아니므로 문서를 작성할 수 없습니다.");
            return null;
        }

        CaseDocument document = new CaseDocument(
                "document-" + System.currentTimeMillis(),
                caseId,
                documentType,
                documentType + " 초안",
                "소속 변호사가 작성한 사건 관련 문서입니다.",
                associateLawyerId
        );

        targetCase.addCaseDocument(document);

        return document;
    }

    public boolean shareCaseProgress(String caseId, ProgressionRecord record) {
        if (caseId == null || caseId.trim().isEmpty()) {
            return false;
        }

        if (record == null) {
            return false;
        }

        Case targetCase = findAssignedCase(caseId);

        if (targetCase == null) {
            System.out.println("담당 사건이 아니므로 진행 상황을 공유할 수 없습니다.");
            return false;
        }

        targetCase.addProgressRecord(record);

        System.out.println("사건 진행 상황을 의뢰인에게 공유했습니다.");
        return true;
    }

    public boolean requestClientMaterial(String caseId, String requestContent) {
        if (caseId == null || caseId.trim().isEmpty()) {
            return false;
        }

        if (requestContent == null || requestContent.trim().isEmpty()) {
            return false;
        }

        Case targetCase = findAssignedCase(caseId);

        if (targetCase == null) {
            System.out.println("담당 사건이 아니므로 자료를 요청할 수 없습니다.");
            return false;
        }

        System.out.println("의뢰인에게 추가 자료를 요청했습니다.");
        System.out.println("사건 ID: " + caseId);
        System.out.println("요청 내용: " + requestContent);

        return true;
    }

    public CaseDocument useAIDraft(String caseId, String documentType) {
        if (caseId == null || caseId.trim().isEmpty()) {
            return null;
        }

        if (documentType == null || documentType.trim().isEmpty()) {
            return null;
        }

        Case targetCase = findAssignedCase(caseId);

        if (targetCase == null) {
            System.out.println("담당 사건이 아니므로 AI 초안을 작성할 수 없습니다.");
            return null;
        }

        CaseDocument draftDocument = new CaseDocument(
                "ai-document-" + System.currentTimeMillis(),
                caseId,
                documentType,
                "[AI 초안] " + documentType,
                "AI가 사건 정보, 증거 자료, 적용 법리를 바탕으로 작성한 문서 초안입니다.",
                associateLawyerId
        );

        targetCase.addCaseDocument(draftDocument);

        return draftDocument;
    }

    public boolean addAssignedCase(Case assignedCase) {
        if (assignedCase == null) {
            return false;
        }

        this.assignedCase.add(assignedCase);
        increaseWorkload();

        return true;
    }

    private Case findAssignedCase(String caseId) {
        if (caseId == null || caseId.trim().isEmpty()) {
            return null;
        }

        for (Case currentCase : assignedCase) {
            if (caseId.equals(currentCase.getCaseId())) {
                return currentCase;
            }
        }

        return null;
    }

    public String getAssociateLawyerId() {
        return associateLawyerId;
    }

    public List<Case> getAssignedCase() {
        return assignedCase;
    }
}
