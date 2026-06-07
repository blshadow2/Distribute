package lawSystem.retainer;

import java.time.LocalDateTime;

public class RetainerCondition {
    private String conditionId;
    private String retainerRequestId;
    private int fee;
    private String scope;
    private String additionalTerms;
    private int revisionNo;
    private ConditionStatus conditionStatus;
    private LocalDateTime createdAt;

    public RetainerCondition(
            String conditionId,
            String retainerRequestId,
            int fee,
            String scope,
            String additionalTerms
    ) {
        this.conditionId = conditionId;
        this.retainerRequestId = retainerRequestId;
        this.fee = fee;
        this.scope = scope;
        this.additionalTerms = additionalTerms;
        this.revisionNo = 1;
        this.conditionStatus = ConditionStatus.CREATED;
        this.createdAt = LocalDateTime.now();
    }

    public static RetainerCondition createCondition(
            int fee,
            String scope,
            String terms
    ) {
        return new RetainerCondition(
                "condition-" + System.currentTimeMillis(),
                null,
                fee,
                scope,
                terms
        );
    }

    public static RetainerCondition createCondition(
            String retainerRequestId,
            int fee,
            String scope,
            String terms
    ) {
        return new RetainerCondition(
                "condition-" + System.currentTimeMillis(),
                retainerRequestId,
                fee,
                scope,
                terms
        );
    }

    public boolean editCondition(
            int fee,
            String scope,
            String terms
    ) {
        if (conditionStatus == ConditionStatus.ACCEPTED
                || conditionStatus == ConditionStatus.REJECTED) {
            return false;
        }

        this.fee = fee;
        this.scope = scope;
        this.additionalTerms = terms;
        this.revisionNo++;
        this.conditionStatus = ConditionStatus.REVISED;
        return validateRequiredFields();
    }

    public boolean validateRequiredFields() {
        return fee > 0
                && scope != null
                && !scope.trim().isEmpty();
    }

    public boolean sendCondition() {
        if (!validateRequiredFields()) {
            return false;
        }

        this.conditionStatus = ConditionStatus.SENT;
        return true;
    }

    public boolean acceptCondition() {
        if (conditionStatus != ConditionStatus.SENT
                && conditionStatus != ConditionStatus.REVISED) {
            return false;
        }

        this.conditionStatus = ConditionStatus.ACCEPTED;
        return true;
    }

    public boolean rejectCondition() {
        if (conditionStatus == ConditionStatus.ACCEPTED) {
            return false;
        }

        this.conditionStatus = ConditionStatus.REJECTED;
        return true;
    }

    public String getConditionId() {
        return conditionId;
    }

    public String getRetainerRequestId() {
        return retainerRequestId;
    }

    public int getFee() {
        return fee;
    }

    public String getScope() {
        return scope;
    }

    public String getAdditionalTerms() {
        return additionalTerms;
    }

    public int getRevisionNo() {
        return revisionNo;
    }

    public ConditionStatus getConditionStatus() {
        return conditionStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setConditionStatus(ConditionStatus conditionStatus) {
        this.conditionStatus = conditionStatus;
    }

    public void setRevisionNo(int revisionNo) {
        this.revisionNo = revisionNo;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setRetainerRequestId(String retainerRequestId) {
        this.retainerRequestId = retainerRequestId;
    }

    @Override
    public String toString() {
        return "수임 조건 ID: " + conditionId +
                "\n수임 요청 ID: " + retainerRequestId +
                "\n수임료: " + fee +
                "\n수임 범위: " + scope +
                "\n기타 조건: " + additionalTerms +
                "\n수정 번호: " + revisionNo +
                "\n수임 조건 상태: " + conditionStatus +
                "\n생성 일시: " + createdAt;
    }
}
