package lawSystem.precedent;

import java.util.ArrayList;
import java.util.List;

/** RAG 서비스 /llm/summarize 응답 DTO. */
public class SummaryDto {

    private final String summary;
    private final List<String> mainIssues;
    private final String timeline;

    public SummaryDto(String summary, List<String> mainIssues, String timeline) {
        this.summary = summary != null ? summary : "";
        this.mainIssues = mainIssues != null ? mainIssues : new ArrayList<>();
        this.timeline = timeline != null ? timeline : "";
    }

    public String getSummary() {
        return summary;
    }

    public List<String> getMainIssues() {
        return mainIssues;
    }

    public String getTimeline() {
        return timeline;
    }
}
