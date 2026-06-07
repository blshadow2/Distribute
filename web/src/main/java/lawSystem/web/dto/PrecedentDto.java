package lawSystem.web.dto;

/** 유사판례 한 건(화면 표시용). */
public class PrecedentDto {

    private String precedentId;
    private String title;
    private double similarityScore;
    private String legalIssue;
    private String summary;

    public PrecedentDto(String precedentId, String title, double similarityScore,
                        String legalIssue, String summary) {
        this.precedentId = precedentId;
        this.title = title;
        this.similarityScore = similarityScore;
        this.legalIssue = legalIssue;
        this.summary = summary;
    }

    public String getPrecedentId() { return precedentId; }
    public String getTitle() { return title; }
    public double getSimilarityScore() { return similarityScore; }
    public String getLegalIssue() { return legalIssue; }
    public String getSummary() { return summary; }
}
