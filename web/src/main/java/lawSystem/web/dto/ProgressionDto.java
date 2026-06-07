package lawSystem.web.dto;

/** 진행상황 표시용. */
public class ProgressionDto {

    private final String progressId;
    private final String caseTitle;
    private final String progressStatus;
    private final String description;
    private final String recentAction;
    private final String requestedMaterial;

    public ProgressionDto(String progressId, String caseTitle, String progressStatus,
                          String description, String recentAction, String requestedMaterial) {
        this.progressId = progressId;
        this.caseTitle = caseTitle;
        this.progressStatus = progressStatus;
        this.description = description;
        this.recentAction = recentAction;
        this.requestedMaterial = requestedMaterial;
    }

    public String getProgressId() { return progressId; }
    public String getCaseTitle() { return caseTitle; }
    public String getProgressStatus() { return progressStatus; }
    public String getDescription() { return description; }
    public String getRecentAction() { return recentAction; }
    public String getRequestedMaterial() { return requestedMaterial; }
}
