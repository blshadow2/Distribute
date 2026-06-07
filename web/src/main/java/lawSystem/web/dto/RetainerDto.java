package lawSystem.web.dto;

/** 수임 요청 + 최신 조건 표시용. */
public class RetainerDto {

    private final String id;
    private final String caseId;
    private final String caseTitle;
    private final String clientName;
    private final String lawyerName;
    private final String requestContent;
    private final String desiredScope;
    private final int desiredFee;
    private final String desiredResult;
    private final String status;
    private final String requestedAt;
    private final String adjustmentNote;

    // 최신 조건
    private final boolean hasCondition;
    private final int condFee;
    private final String condScope;
    private final String condTerms;
    private final String condStatus;

    public RetainerDto(String id, String caseId, String caseTitle, String clientName, String lawyerName,
                       String requestContent, String desiredScope, int desiredFee, String desiredResult,
                       String status, String requestedAt, String adjustmentNote,
                       boolean hasCondition, int condFee, String condScope, String condTerms, String condStatus) {
        this.id = id;
        this.caseId = caseId;
        this.caseTitle = caseTitle;
        this.clientName = clientName;
        this.lawyerName = lawyerName;
        this.requestContent = requestContent;
        this.desiredScope = desiredScope;
        this.desiredFee = desiredFee;
        this.desiredResult = desiredResult;
        this.status = status;
        this.requestedAt = requestedAt;
        this.adjustmentNote = adjustmentNote;
        this.hasCondition = hasCondition;
        this.condFee = condFee;
        this.condScope = condScope;
        this.condTerms = condTerms;
        this.condStatus = condStatus;
    }

    public String getId() { return id; }
    public String getCaseId() { return caseId; }
    public String getCaseTitle() { return caseTitle; }
    public String getClientName() { return clientName; }
    public String getLawyerName() { return lawyerName; }
    public String getRequestContent() { return requestContent; }
    public String getDesiredScope() { return desiredScope; }
    public int getDesiredFee() { return desiredFee; }
    public String getDesiredResult() { return desiredResult; }
    public String getStatus() { return status; }
    public String getRequestedAt() { return requestedAt; }
    public String getAdjustmentNote() { return adjustmentNote; }
    public boolean isHasCondition() { return hasCondition; }
    public int getCondFee() { return condFee; }
    public String getCondScope() { return condScope; }
    public String getCondTerms() { return condTerms; }
    public String getCondStatus() { return condStatus; }
}
