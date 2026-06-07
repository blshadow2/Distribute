package lawSystem.retainer;

import java.time.LocalDateTime;

public class RetainerRequest {
    private String retainerRequestId;
    private String caseId;
    private String clientId;
    private String lawyerId;
    private String requestContent;
    private String desiredScope;
    private int desiredFee;
    private String desiredResult;
    private RetainerStatus requestStatus;
    private LocalDateTime requestedAt;

    public RetainerRequest(
            String retainerRequestId,
            String caseId,
            String clientId,
            String lawyerId,
            String requestContent,
            String desiredScope,
            int desiredFee,
            String desiredResult
    ) {
        this.retainerRequestId = retainerRequestId;
        this.caseId = caseId;
        this.clientId = clientId;
        this.lawyerId = lawyerId;
        this.requestContent = requestContent;
        this.desiredScope = desiredScope;
        this.desiredFee = desiredFee;
        this.desiredResult = desiredResult;
        this.requestStatus = RetainerStatus.CREATED;
        this.requestedAt = LocalDateTime.now();
    }

    public static RetainerRequest createRetainerRequest(
            String caseId,
            String lawyerId
    ) {
        return new RetainerRequest(
                "lawSystem.retainer-" + System.currentTimeMillis(),
                caseId,
                null,
                lawyerId,
                "",
                "",
                0,
                ""
        );
    }

    public static RetainerRequest createRetainerRequest(
            String caseId,
            String clientId,
            String lawyerId,
            String requestContent,
            String desiredScope,
            int desiredFee,
            String desiredResult
    ) {
        return new RetainerRequest(
                "lawSystem.retainer-" + System.currentTimeMillis(),
                caseId,
                clientId,
                lawyerId,
                requestContent,
                desiredScope,
                desiredFee,
                desiredResult
        );
    }

    public boolean sendRequest() {
        if (!validateRequiredFields()) {
            return false;
        }

        this.requestStatus = RetainerStatus.REQUEST_SENT;
        return true;
    }

    public void updateRequestStatus(RetainerStatus status) {
        if (status != null) {
            this.requestStatus = status;
        }
    }

    public boolean acceptCondition(String conditionId) {
        if (conditionId == null || conditionId.trim().isEmpty()) {
            return false;
        }

        if (requestStatus == RetainerStatus.REJECTED) {
            return false;
        }

        this.requestStatus = RetainerStatus.CONDITION_ACCEPTED;
        return true;
    }

    public boolean requestConditionAdjustment(String message) {
        if (message == null || message.trim().isEmpty()) {
            return false;
        }

        if (requestStatus == RetainerStatus.REJECTED) {
            return false;
        }

        this.requestStatus = RetainerStatus.CONDITION_ADJUSTMENT_REQUESTED;
        this.requestContent = message;
        return true;
    }

    public boolean rejectCondition(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            return false;
        }

        this.requestStatus = RetainerStatus.REJECTED;
        this.requestContent = reason;
        return true;
    }

    private boolean validateRequiredFields() {
        return caseId != null && !caseId.trim().isEmpty()
                && lawyerId != null && !lawyerId.trim().isEmpty()
                && desiredScope != null && !desiredScope.trim().isEmpty()
                && desiredFee >= 0;
    }

    public String getRetainerRequestId() {
        return retainerRequestId;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getLawyerId() {
        return lawyerId;
    }

    public String getRequestContent() {
        return requestContent;
    }

    public String getDesiredScope() {
        return desiredScope;
    }

    public int getDesiredFee() {
        return desiredFee;
    }

    public String getDesiredResult() {
        return desiredResult;
    }

    public RetainerStatus getRequestStatus() {
        return requestStatus;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    @Override
    public String toString() {
        return "수임 요청 ID: " + retainerRequestId +
                "\n사건 ID: " + caseId +
                "\n의뢰인 ID: " + clientId +
                "\n변호사 ID: " + lawyerId +
                "\n요청 내용: " + requestContent +
                "\n희망 수임 범위: " + desiredScope +
                "\n희망 수임료: " + desiredFee +
                "\n희망 결과: " + desiredResult +
                "\n수임 요청 상태: " + requestStatus +
                "\n요청 일시: " + requestedAt;
    }
}
