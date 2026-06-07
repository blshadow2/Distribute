package lawSystem.ai;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CaseAnalysisReport extends AIAnalysisResult {
    private String reportId;
    private String summary;
    private List<String> mainIssues;
    private String timeline;
    private List<String> relatedEvidenceIds;

    public CaseAnalysisReport(
            String aiResultId,
            String aiRequestId,
            String caseId,
            String reportId,
            String summary,
            List<String> mainIssues,
            String timeline,
            List<String> relatedEvidenceIds,
            double confidenceScore
    ) {
        super(aiResultId, aiRequestId, caseId, AnalysisType.CASE_SUMMARY, summary, confidenceScore);
        this.reportId = reportId;
        this.summary = summary;
        this.mainIssues = mainIssues != null ? mainIssues : new ArrayList<>();
        this.timeline = timeline;
        this.relatedEvidenceIds = relatedEvidenceIds != null ? relatedEvidenceIds : new ArrayList<>();
    }

    public static CaseAnalysisReport generateReport(String caseId) {
        List<String> issues = new ArrayList<>();
        issues.add("사실관계 확인");
        issues.add("증거자료의 신빙성");
        issues.add("법적 책임 성립 여부");

        return new CaseAnalysisReport(
                "ai-result-" + System.currentTimeMillis(),
                "ai-request-dummy-" + System.currentTimeMillis(),
                caseId,
                "report-" + System.currentTimeMillis(),
                "사건의 사실관계와 주요 쟁점을 요약한 더미 리포트입니다.",
                issues,
                "사건 접수 → 증거 등록 → 변호사 검토 → 문서 작성",
                new ArrayList<>(),
                0.85
        );
    }

    public void updateSummary(String summary) {
        this.summary = summary;
        updateResult(summary);
    }

    public boolean addIssue(String issue) {
        if (issue == null || issue.trim().isEmpty()) {
            return false;
        }

        return mainIssues.add(issue);
    }

    public boolean linkEvidence(String evidenceId) {
        if (evidenceId == null || evidenceId.trim().isEmpty()) {
            return false;
        }

        return relatedEvidenceIds.add(evidenceId);
    }

    public File exportReport() {
        return new File(reportId + ".txt");
    }

    public String getReportId() {
        return reportId;
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

    public List<String> getRelatedEvidenceIds() {
        return relatedEvidenceIds;
    }
}