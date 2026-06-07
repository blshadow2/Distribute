package lawSystem.legalCase;

public class ProgressionRecord {
    private String progressId;
    private String caseId;
    private String writerId;
    private String progressStatus;
    private String description;
    private String recentAction;
    private String requestedMaterial;

    public ProgressionRecord(
            String progressId,
            String caseId,
            String writerId,
            String progressStatus,
            String description,
            String recentAction
    ) {
        this.progressId = progressId;
        this.caseId = caseId;
        this.writerId = writerId;
        this.progressStatus = progressStatus;
        this.description = description;
        this.recentAction = recentAction;
    }

    public static ProgressionRecord createProgressRecord(
            String caseId,
            String writerId,
            String progressStatus,
            String description,
            String recentAction
    ) {
        return new ProgressionRecord(
                "progress-" + System.currentTimeMillis(),
                caseId,
                writerId,
                progressStatus,
                description,
                recentAction
        );
    }

    public boolean updateProgressRecord(
            String progressStatus,
            String description,
            String recentAction
    ) {
        if (progressStatus == null || progressStatus.trim().isEmpty()) {
            return false;
        }

        this.progressStatus = progressStatus;
        this.description = description;
        this.recentAction = recentAction;
        return true;
    }

    public boolean shareWithClient(String clientId) {
        return clientId != null && !clientId.trim().isEmpty();
    }

    public boolean requestAdditionalMaterial(String materialName) {
        if (materialName == null || materialName.trim().isEmpty()) {
            return false;
        }

        this.requestedMaterial = materialName;
        return true;
    }

    public String getProgressId() {
        return progressId;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getProgressStatus() {
        return progressStatus;
    }

    public String getDescription() {
        return description;
    }

    public String getRequestedMaterial() {
        return requestedMaterial;
    }

    public String getWriterId() {
        return writerId;
    }

    public String getRecentAction() {
        return recentAction;
    }

    public void setRequestedMaterial(String requestedMaterial) {
        this.requestedMaterial = requestedMaterial;
    }
}