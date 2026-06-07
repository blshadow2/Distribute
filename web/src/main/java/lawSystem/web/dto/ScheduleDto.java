package lawSystem.web.dto;

/** 상담 일정 표시용. */
public class ScheduleDto {

    private final String scheduleId;
    private final String lawyerId;
    private final String lawyerName;
    private final String dateTime;
    private final int duration;
    private final String status;

    public ScheduleDto(String scheduleId, String lawyerId, String lawyerName,
                       String dateTime, int duration, String status) {
        this.scheduleId = scheduleId;
        this.lawyerId = lawyerId;
        this.lawyerName = lawyerName;
        this.dateTime = dateTime;
        this.duration = duration;
        this.status = status;
    }

    public String getScheduleId() { return scheduleId; }
    public String getLawyerId() { return lawyerId; }
    public String getLawyerName() { return lawyerName; }
    public String getDateTime() { return dateTime; }
    public int getDuration() { return duration; }
    public String getStatus() { return status; }
}
