package lawSystem.ai;


import java.util.ArrayList;
import java.util.List;

public class LegalRuleAnalysisResult extends AIAnalysisResult {
    private String legalAnalysisId;
    private String documentId;
    private String issueSummary;
    private String applicableLaw;
    private String legalExplanation;
    private List<String> relatedStatutes;

    public LegalRuleAnalysisResult(
            String aiResultId,
            String aiRequestId,
            String caseId,
            String legalAnalysisId,
            String documentId,
            String issueSummary,
            String applicableLaw,
            String legalExplanation,
            List<String> relatedStatutes,
            double confidenceScore
    ) {
        super(aiResultId, aiRequestId, caseId, AnalysisType.LEGAL_RULE_ANALYSIS, legalExplanation, confidenceScore);
        this.legalAnalysisId = legalAnalysisId;
        this.documentId = documentId;
        this.issueSummary = issueSummary;
        this.applicableLaw = applicableLaw;
        this.legalExplanation = legalExplanation;
        this.relatedStatutes = relatedStatutes != null ? relatedStatutes : new ArrayList<>();
    }

    public static LegalRuleAnalysisResult createLegalAnalysis(
            String caseId,
            String documentId
    ) {
        List<String> statutes = new ArrayList<>();
        statutes.add("민법 제390조");
        statutes.add("민법 제750조");

        return new LegalRuleAnalysisResult(
                "ai-result-" + System.currentTimeMillis(),
                "ai-request-dummy-" + System.currentTimeMillis(),
                caseId,
                "legal-analysis-" + System.currentTimeMillis(),
                documentId,
                "계약 위반과 손해 발생의 관련성",
                "손해배상 책임",
                "선택한 문서에서 손해배상 책임과 관련된 법리를 설명한 더미 결과입니다.",
                statutes,
                0.82
        );
    }

    public boolean updateExplanation(String explanation) {
        if (explanation == null || explanation.trim().isEmpty()) {
            return false;
        }

        this.legalExplanation = explanation;
        updateResult(explanation);
        return true;
    }

    public boolean addRelatedStatute(String statute) {
        if (statute == null || statute.trim().isEmpty()) {
            return false;
        }

        return relatedStatutes.add(statute);
    }

    public String renderForClient() {
        return "쟁점: " + issueSummary +
                "\n적용 법리: " + applicableLaw +
                "\n설명: " + legalExplanation +
                "\n관련 법령: " + relatedStatutes;
    }

    public boolean saveLegalAnalysis() {
        return saveResult();
    }

    public String getLegalAnalysisId() {
        return legalAnalysisId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getIssueSummary() {
        return issueSummary;
    }

    public String getApplicableLaw() {
        return applicableLaw;
    }

    public String getLegalExplanation() {
        return legalExplanation;
    }

    public List<String> getRelatedStatutes() {
        return relatedStatutes;
    }
}
