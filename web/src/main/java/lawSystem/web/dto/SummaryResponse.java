package lawSystem.web.dto;

import java.util.List;

/** POST /api/ai/summary 응답 (저장된 AI 요약 결과). */
public class SummaryResponse {

    private String aiResultId;
    private String summary;
    private List<String> mainIssues;
    private String timeline;
    private double confidenceScore;

    public SummaryResponse(String aiResultId, String summary, List<String> mainIssues,
                           String timeline, double confidenceScore) {
        this.aiResultId = aiResultId;
        this.summary = summary;
        this.mainIssues = mainIssues;
        this.timeline = timeline;
        this.confidenceScore = confidenceScore;
    }

    public String getAiResultId() { return aiResultId; }
    public String getSummary() { return summary; }
    public List<String> getMainIssues() { return mainIssues; }
    public String getTimeline() { return timeline; }
    public double getConfidenceScore() { return confidenceScore; }
}
