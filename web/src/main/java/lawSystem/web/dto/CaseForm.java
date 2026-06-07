package lawSystem.web.dto;

/** 사건 등록 폼 입력. */
public class CaseForm {

    private String title;
    private String category;        // CaseCategory 이름 (CIVIL, CRIMINAL, ...)
    private String currentStage;
    private String factDescription;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCurrentStage() { return currentStage; }
    public void setCurrentStage(String currentStage) { this.currentStage = currentStage; }

    public String getFactDescription() { return factDescription; }
    public void setFactDescription(String factDescription) { this.factDescription = factDescription; }
}
