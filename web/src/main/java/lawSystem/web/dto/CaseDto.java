package lawSystem.web.dto;

import java.time.LocalDateTime;
import java.util.List;

/** 사건 조회 응답(목록/상세/AI 선택). */
public class CaseDto {

    private final String caseId;
    private final String title;
    private final String category;
    private final String status;
    private final String currentStage;
    private final String factDescription;
    private final LocalDateTime createdAt;
    private final List<String> keywords;

    public CaseDto(String caseId, String title, String category, String status,
                   String currentStage, String factDescription, LocalDateTime createdAt,
                   List<String> keywords) {
        this.caseId = caseId;
        this.title = title;
        this.category = category;
        this.status = status;
        this.currentStage = currentStage;
        this.factDescription = factDescription;
        this.createdAt = createdAt;
        this.keywords = keywords;
    }

    public List<String> getKeywords() { return keywords; }

    public String getCaseId() { return caseId; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getStatus() { return status; }
    public String getCurrentStage() { return currentStage; }
    public String getFactDescription() { return factDescription; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
