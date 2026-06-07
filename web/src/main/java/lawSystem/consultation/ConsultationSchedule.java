package lawSystem.consultation;

import java.time.LocalDateTime;

public class ConsultationSchedule {
    private String scheduleId;
    private String lawyerId;
    private LocalDateTime dateTime;
    private int duration;
    private ScheduleStatus availableStatus;

    public ConsultationSchedule(
            String scheduleId,
            String lawyerId,
            LocalDateTime dateTime,
            int duration
    ) {
        this.scheduleId = scheduleId;
        this.lawyerId = lawyerId;
        this.dateTime = dateTime;
        this.duration = duration;
        this.availableStatus = ScheduleStatus.AVAILABLE;
    }

    public ConsultationSchedule registerSchedule(
            LocalDateTime dateTime,
            int duration
    ) {
        if (dateTime == null || duration <= 0) {
            return null;
        }

        this.dateTime = dateTime;
        this.duration = duration;
        this.availableStatus = ScheduleStatus.AVAILABLE;

        return this;
    }

    public boolean updateSchedule(
            LocalDateTime dateTime,
            int duration
    ) {
        if (dateTime == null || duration <= 0) {
            return false;
        }

        if (availableStatus == ScheduleStatus.RESERVED) {
            return false;
        }

        this.dateTime = dateTime;
        this.duration = duration;

        return true;
    }

    public void markAvailable() {
        this.availableStatus = ScheduleStatus.AVAILABLE;
    }

    public boolean reserveSchedule() {
        if (availableStatus != ScheduleStatus.AVAILABLE) {
            return false;
        }

        this.availableStatus = ScheduleStatus.RESERVED;
        return true;
    }

    public boolean cancelSchedule() {
        if (availableStatus == ScheduleStatus.CANCELED) {
            return false;
        }

        this.availableStatus = ScheduleStatus.CANCELED;
        return true;
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public String getLawyerId() {
        return lawyerId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public int getDuration() {
        return duration;
    }

    public ScheduleStatus getAvailableStatus() {
        return availableStatus;
    }

    public void setAvailableStatus(ScheduleStatus availableStatus) {
        this.availableStatus = availableStatus;
    }

    @Override
    public String toString() {
        return "상담 일정 ID: " + scheduleId +
                "\n변호사 ID: " + lawyerId +
                "\n상담 일시: " + dateTime +
                "\n상담 시간: " + duration + "분" +
                "\n상담 가능 상태: " + availableStatus;
    }
}