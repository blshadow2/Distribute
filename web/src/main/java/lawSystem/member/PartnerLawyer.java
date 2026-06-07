package lawSystem.member;

import lawSystem.retainer.RetainerCondition;
import lawSystem.retainer.RetainerRequest;
import lawSystem.retainer.RetainerStatus;

import java.util.ArrayList;
import java.util.List;

public class PartnerLawyer extends Lawyer {
    private String managingLawyerId;

    public PartnerLawyer(
            String memberId,
            String lawyerId,
            String managingLawyerId,
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

        this.managingLawyerId = managingLawyerId;
    }

    public boolean sendRetainerCondition(String requestId, RetainerCondition condition) {
        if (requestId == null || requestId.trim().isEmpty()) {
            return false;
        }

        if (condition == null) {
            return false;
        }

        RetainerRequest request = findRetainerRequest(requestId);

        if (request == null) {
            System.out.println("해당 수임 요청을 찾을 수 없습니다.");
            return false;
        }

        boolean conditionSent = condition.sendCondition();

        if (!conditionSent) {
            System.out.println("수임 조건의 필수 항목이 누락되어 전송할 수 없습니다.");
            return false;
        }

        request.updateRequestStatus(RetainerStatus.CONDITION_SENT);

        System.out.println("수임 조건을 의뢰인에게 전달했습니다.");
        System.out.println(condition);

        return true;
    }

    public boolean acceptRetainer(String requestId) {
        RetainerRequest request = findRetainerRequest(requestId);

        if (request == null) {
            System.out.println("해당 수임 요청을 찾을 수 없습니다.");
            return false;
        }

        if (request.getRequestStatus() != RetainerStatus.CONDITION_ACCEPTED) {
            System.out.println("수임 조건에 동의된 요청만 수임할 수 있습니다.");
            return false;
        }

        request.updateRequestStatus(RetainerStatus.RETAINED);

        System.out.println("사건을 수임 처리했습니다.");
        return true;
    }

    public boolean approveRetainerDocument(String documentId) {
        if (documentId == null || documentId.trim().isEmpty()) {
            return false;
        }

        System.out.println("수임 문서를 최종 승인했습니다.");
        System.out.println("문서 ID: " + documentId);

        return true;
    }

    public boolean assignCase(String caseId, String lawyerId) {
        if (caseId == null || caseId.trim().isEmpty()) {
            return false;
        }

        if (lawyerId == null || lawyerId.trim().isEmpty()) {
            return false;
        }

        System.out.println("사건을 소속 변호사에게 배당했습니다.");
        System.out.println("사건 ID: " + caseId);
        System.out.println("담당 소속 변호사 ID: " + lawyerId);

        return true;
    }

    public List<String> requestAssignmentRecommendation(String caseId) {
        List<String> recommendationResults = new ArrayList<>();

        if (caseId == null || caseId.trim().isEmpty()) {
            return recommendationResults;
        }

        /*
         * 문서의 원래 타입은 List<AIAnalysisResult>에 가깝지만,
         * 아직 AIAnalysisResult 클래스를 작성하지 않았으므로
         * 현재는 텍스트 기반 테스트를 위해 List<String>으로 둔다.
         */
        recommendationResults.add("추천 1: 현재 업무량이 낮은 소속 변호사");
        recommendationResults.add("추천 2: 사건 분야와 전문 분야가 일치하는 소속 변호사");
        recommendationResults.add("추천 3: 과거 유사 사건 처리 경험이 있는 소속 변호사");

        return recommendationResults;
    }

    public String getManagingLawyerId() {
        return managingLawyerId;
    }
}
