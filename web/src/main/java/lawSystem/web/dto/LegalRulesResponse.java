package lawSystem.web.dto;

import java.util.List;

/** POST /api/ai/legal-rules 응답 (RAG 결합 법리 분석). */
public class LegalRulesResponse {

    private String issueSummary;
    private String applicableLaw;
    private String legalExplanation;
    private List<String> relatedStatutes;
    private List<String> citedCases;

    public LegalRulesResponse(String issueSummary, String applicableLaw, String legalExplanation,
                              List<String> relatedStatutes, List<String> citedCases) {
        this.issueSummary = issueSummary;
        this.applicableLaw = applicableLaw;
        this.legalExplanation = legalExplanation;
        this.relatedStatutes = relatedStatutes;
        this.citedCases = citedCases;
    }

    public String getIssueSummary() { return issueSummary; }
    public String getApplicableLaw() { return applicableLaw; }
    public String getLegalExplanation() { return legalExplanation; }
    public List<String> getRelatedStatutes() { return relatedStatutes; }
    public List<String> getCitedCases() { return citedCases; }
}
