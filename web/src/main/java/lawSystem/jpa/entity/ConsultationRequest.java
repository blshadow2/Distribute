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
import lawSystem.consultation.ConsultationStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "consultation_request")
public class ConsultationRequest {

    @Id
    @Column(name = "consultation_request_id", length = 64, nullable = false, updatable = false)
    private String consultationRequestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    private LegalCase legalCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", referencedColumnName = "member_id")
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lawyer_id", referencedColumnName = "member_id", nullable = false)
    private Lawyer lawyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private ConsultationSchedule schedule;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status", length = 30, nullable = false)
    private ConsultationStatus requestStatus = ConsultationStatus.REQUESTED;

    @Column(name = "request_memo", columnDefinition = "TEXT")
    private String requestMemo;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt = LocalDateTime.now();

    protected ConsultationRequest() {}

    public ConsultationRequest(String id, LegalCase legalCase, Client client, Lawyer lawyer,
                               ConsultationSchedule schedule, String memo) {
        this.consultationRequestId = id;
        this.legalCase = legalCase;
        this.client = client;
        this.lawyer = lawyer;
        this.schedule = schedule;
        this.requestMemo = memo;
    }

    public String getConsultationRequestId() { return consultationRequestId; }
    public LegalCase getLegalCase() { return legalCase; }
    public Client getClient() { return client; }
    public Lawyer getLawyer() { return lawyer; }
    public ConsultationSchedule getSchedule() { return schedule; }
    public ConsultationStatus getRequestStatus() { return requestStatus; }
    public String getRequestMemo() { return requestMemo; }
    public LocalDateTime getRequestedAt() { return requestedAt; }

    public void setRequestStatus(ConsultationStatus requestStatus) { this.requestStatus = requestStatus; }
    public void setSchedule(ConsultationSchedule schedule) { this.schedule = schedule; }
}
