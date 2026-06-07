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
import lawSystem.retainer.ConditionStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "retainer_condition")
public class RetainerCondition {

    @Id
    @Column(name = "condition_id", length = 64, nullable = false, updatable = false)
    private String conditionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "retainer_request_id", nullable = false)
    private RetainerRequest retainerRequest;

    @Column(name = "fee", nullable = false)
    private int fee;

    @Column(name = "scope", length = 255)
    private String scope;

    @Column(name = "additional_terms", columnDefinition = "TEXT")
    private String additionalTerms;

    @Column(name = "revision_no", nullable = false)
    private int revisionNo = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_status", length = 30, nullable = false)
    private ConditionStatus conditionStatus = ConditionStatus.CREATED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected RetainerCondition() {}

    public RetainerCondition(String conditionId, int fee, String scope, String additionalTerms) {
        this.conditionId = conditionId;
        this.fee = fee;
        this.scope = scope;
        this.additionalTerms = additionalTerms;
    }

    public String getConditionId() { return conditionId; }
    public RetainerRequest getRetainerRequest() { return retainerRequest; }
    public int getFee() { return fee; }
    public String getScope() { return scope; }
    public String getAdditionalTerms() { return additionalTerms; }
    public int getRevisionNo() { return revisionNo; }
    public ConditionStatus getConditionStatus() { return conditionStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setRetainerRequest(RetainerRequest retainerRequest) { this.retainerRequest = retainerRequest; }
    public void setConditionStatus(ConditionStatus s) { this.conditionStatus = s; }
}
