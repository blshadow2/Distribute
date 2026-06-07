package lawSystem.ai;

public class PrecedentAnalysisResult extends AIAnalysisResult {
    private String precedentResultId;
    private String precedentId;
    private String precedentTitle;
    private String precedentSummary;
    private double similarityScore;
    private String legalIssue;

    public PrecedentAnalysisResult(
            String aiResultId,
            String aiRequestId,
            String caseId,
            String precedentResultId,
            String precedentId,
            String precedentTitle,
            String precedentSummary,
            double similarityScore,
            String legalIssue
    ) {
        super(aiResultId, aiRequestId, caseId, AnalysisType.SIMILAR_PRECEDENTS, precedentSummary, similarityScore);
        this.precedentResultId = precedentResultId;
        this.precedentId = precedentId;
        this.precedentTitle = precedentTitle;
        this.precedentSummary = precedentSummary;
        this.similarityScore = similarityScore;
        this.legalIssue = legalIssue;
    }

    public static PrecedentAnalysisResult searchSimilarPrecedents(
            String caseId,
            String precedentTitle
    ) {
        return new PrecedentAnalysisResult(
                "ai-result-" + System.currentTimeMillis(),
                "ai-request-dummy-" + System.currentTimeMillis(),
                caseId,
                "precedent-result-" + System.currentTimeMillis(),
                "precedent-" + System.currentTimeMillis(),
                precedentTitle,
                "현재 사건과 사실관계가 유사한 판례 더미 요약입니다.",
                0.81,
                "손해배상 책임"
        );
    }

    public double calculateSimilarity(String caseId, String precedentId) {
        if (caseId == null || precedentId == null) {
            return 0.0;
        }

        return similarityScore;
    }

    public boolean selectPrecedent(String precedentId) {
        return this.precedentId.equals(precedentId);
    }

    public boolean savePrecedentResult() {
        return saveResult();
    }

    public String getPrecedentResultId() {
        return precedentResultId;
    }

    public String getPrecedentId() {
        return precedentId;
    }

    public String getPrecedentTitle() {
        return precedentTitle;
    }

    public String getPrecedentSummary() {
        return precedentSummary;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    public String getLegalIssue() {
        return legalIssue;
    }
}
