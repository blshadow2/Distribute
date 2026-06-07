package lawSystem.jpa.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lawSystem.retainer.RetainerStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "retainer_request")
public class RetainerRequest {

    @Id
    @Column(name = "retainer_request_id", length = 64, nullable = false, updatable = false)
    private String retainerRequestId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false)
    private LegalCase legalCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", referencedColumnName = "member_id")
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lawyer_id", referencedColumnName = "member_id", nullable = false)
    private Lawyer lawyer;

    @Column(name = "request_content", columnDefinition = "TEXT")
    private String requestContent;

    @Column(name = "desired_scope", length = 255)
    private String desiredScope;

    @Column(name = "desired_fee")
    private int desiredFee;

    @Column(name = "desired_result", columnDefinition = "TEXT")
    private String desiredResult;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status", length = 40, nullable = false)
    private RetainerStatus requestStatus = RetainerStatus.CREATED;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt = LocalDateTime.now();

    @Column(name = "adjustment_note", columnDefinition = "TEXT")
    private String adjustmentNote;

    @OneToMany(mappedBy = "retainerRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RetainerCondition> conditions = new ArrayList<>();

    protected RetainerRequest() {}

    public RetainerRequest(String id, LegalCase legalCase, Client client, Lawyer lawyer,
                           String requestContent, String desiredScope, int desiredFee, String desiredResult) {
        this.retainerRequestId = id;
        this.legalCase = legalCase;
        this.client = client;
        this.lawyer = lawyer;
        this.requestContent = requestContent;
        this.desiredScope = desiredScope;
        this.desiredFee = desiredFee;
        this.desiredResult = desiredResult;
    }

    public void addCondition(RetainerCondition condition) {
        conditions.add(condition);
        condition.setRetainerRequest(this);
    }

    public String getRetainerRequestId() { return retainerRequestId; }
    public LegalCase getLegalCase() { return legalCase; }
    public Client getClient() { return client; }
    public Lawyer getLawyer() { return lawyer; }
    public String getRequestContent() { return requestContent; }
    public String getDesiredScope() { return desiredScope; }
    public int getDesiredFee() { return desiredFee; }
    public String getDesiredResult() { return desiredResult; }
    public RetainerStatus getRequestStatus() { return requestStatus; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public List<RetainerCondition> getConditions() { return conditions; }
    public String getAdjustmentNote() { return adjustmentNote; }

    public void setRequestStatus(RetainerStatus requestStatus) { this.requestStatus = requestStatus; }
    public void setAdjustmentNote(String adjustmentNote) { this.adjustmentNote = adjustmentNote; }
}
