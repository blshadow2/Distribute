package lawSystem.web.dto;

/** 상담 신청 표시용. */
public class ConsultationDto {

    private final String id;
    private final String caseTitle;
    private final String clientName;
    private final String lawyerName;
    private final String scheduleTime;
    private final String status;
    private final String memo;
    private final String requestedAt;

    public ConsultationDto(String id, String caseTitle, String clientName, String lawyerName,
                           String scheduleTime, String status, String memo, String requestedAt) {
        this.id = id;
        this.caseTitle = caseTitle;
        this.clientName = clientName;
        this.lawyerName = lawyerName;
        this.scheduleTime = scheduleTime;
        this.status = status;
        this.memo = memo;
        this.requestedAt = requestedAt;
    }

    public String getId() { return id; }
    public String getCaseTitle() { return caseTitle; }
    public String getClientName() { return clientName; }
    public String getLawyerName() { return lawyerName; }
    public String getScheduleTime() { return scheduleTime; }
    public String getStatus() { return status; }
    public String getMemo() { return memo; }
    public String getRequestedAt() { return requestedAt; }
}
