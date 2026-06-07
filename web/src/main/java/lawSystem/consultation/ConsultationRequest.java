package lawSystem.consultation;

import java.time.LocalDateTime;

public class ConsultationRequest {
    private String consultationRequestId;
    private String caseId;
    private String clientId;
    private String lawyerId;
    private String scheduleId;
    private ConsultationStatus requestStatus;
    private String requestMemo;
    private LocalDateTime requestedAt;

    public ConsultationRequest(
            String consultationRequestId,
            String caseId,
            String clientId,
            String lawyerId,
            String scheduleId,
            String requestMemo
    ) {
        this.consultationRequestId = consultationRequestId;
        this.caseId = caseId;
        this.clientId = clientId;
        this.lawyerId = lawyerId;
        this.scheduleId = scheduleId;
        this.requestMemo = requestMemo;
        this.requestStatus = ConsultationStatus.REQUESTED;
        this.requestedAt = LocalDateTime.now();
    }

    public static ConsultationRequest createConsultationRequest(
            String caseId,
            String lawyerId,
            String scheduleId
    ) {
        return new ConsultationRequest(
                "lawSystem.consultation-" + System.currentTimeMillis(),
                caseId,
                null,
                lawyerId,
                scheduleId,
                ""
        );
    }

    public static ConsultationRequest createConsultationRequest(
            String caseId,
            String clientId,
            String lawyerId,
            String scheduleId,
            String requestMemo
    ) {
        return new ConsultationRequest(
                "lawSystem.consultation-" + System.currentTimeMillis(),
                caseId,
                clientId,
                lawyerId,
                scheduleId,
                requestMemo
        );
    }

    public boolean changeSchedule(String scheduleId) {
        if (scheduleId == null || scheduleId.trim().isEmpty()) {
            return false;
        }

        if (requestStatus == ConsultationStatus.CANCELED) {
            return false;
        }

        this.scheduleId = scheduleId;
        this.requestStatus = ConsultationStatus.SCHEDULE_CHANGED;
        return true;
    }

    public boolean approveRequest() {
        if (requestStatus == ConsultationStatus.CANCELED) {
            return false;
        }

        this.requestStatus = ConsultationStatus.APPROVED;
        return true;
    }

    public boolean rejectRequest() {
        if (requestStatus == ConsultationStatus.CANCELED) {
            return false;
        }

        this.requestStatus = ConsultationStatus.REJECTED;
        return true;
    }

    public boolean cancelRequest() {
        if (requestStatus == ConsultationStatus.APPROVED) {
            return false;
        }

        this.requestStatus = ConsultationStatus.CANCELED;
        return true;
    }

    public String getConsultationRequestId() {
        return consultationRequestId;
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

    public String getScheduleId() {
        return scheduleId;
    }

    public ConsultationStatus getRequestStatus() {
        return requestStatus;
    }

    public String getRequestMemo() {
        return requestMemo;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestStatus(ConsultationStatus requestStatus) {
        this.requestStatus = requestStatus;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    @Override
    public String toString() {
        return "상담 신청 ID: " + consultationRequestId +
                "\n사건 ID: " + caseId +
                "\n의뢰인 ID: " + clientId +
                "\n변호사 ID: " + lawyerId +
                "\n상담 일정 ID: " + scheduleId +
                "\n상담 신청 상태: " + requestStatus +
                "\n요청 메모: " + requestMemo +
                "\n요청 일시: " + requestedAt;
    }
}