package lawSystem.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lawSystem.consultation.ScheduleStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "consultation_schedule")
public class ConsultationSchedule {

    @Id
    @Column(name = "schedule_id", length = 64, nullable = false, updatable = false)
    private String scheduleId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lawyer_id", referencedColumnName = "member_id", nullable = false)
    private Lawyer lawyer;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    @Column(name = "duration", nullable = false)
    private int duration;

    @Enumerated(EnumType.STRING)
    @Column(name = "available_status", length = 20, nullable = false)
    private ScheduleStatus availableStatus = ScheduleStatus.AVAILABLE;

    protected ConsultationSchedule() {}

    public ConsultationSchedule(String scheduleId, Lawyer lawyer, LocalDateTime dateTime, int duration) {
        this.scheduleId = scheduleId;
        this.lawyer = lawyer;
        this.dateTime = dateTime;
        this.duration = duration;
    }

    public String getScheduleId() { return scheduleId; }
    public Lawyer getLawyer() { return lawyer; }
    public LocalDateTime getDateTime() { return dateTime; }
    public int getDuration() { return duration; }
    public ScheduleStatus getAvailableStatus() { return availableStatus; }

    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
    public void setDuration(int duration) { this.duration = duration; }
    public void setAvailableStatus(ScheduleStatus availableStatus) { this.availableStatus = availableStatus; }
}
