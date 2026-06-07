package lawSystem.web.dto;

/** 사건 배당 화면의 한 행(사건 + 현재 담당). */
public class CaseAssignmentDto {

    private final String caseId;
    private final String title;
    private final String category;
    private final String status;
    private final String clientName;
    private final String assignedLawyerId;
    private final String assignedLawyerName;

    public CaseAssignmentDto(String caseId, String title, String category, String status,
                             String clientName, String assignedLawyerId, String assignedLawyerName) {
        this.caseId = caseId;
        this.title = title;
        this.category = category;
        this.status = status;
        this.clientName = clientName;
        this.assignedLawyerId = assignedLawyerId;
        this.assignedLawyerName = assignedLawyerName;
    }

    public String getCaseId() { return caseId; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getStatus() { return status; }
    public String getClientName() { return clientName; }
    public String getAssignedLawyerId() { return assignedLawyerId; }
    public String getAssignedLawyerName() { return assignedLawyerName; }
}
