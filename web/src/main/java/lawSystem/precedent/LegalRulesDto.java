package lawSystem.precedent;

import java.util.ArrayList;
import java.util.List;

/** RAG 서비스 /rag/analyze-rules 응답 DTO (RAG 결합 법리 분석). */
public class LegalRulesDto {

    private final String issueSummary;
    private final String applicableLaw;
    private final String legalExplanation;
    private final List<String> relatedStatutes;
    private final List<String> citedCases;   // external_case_id 목록

    public LegalRulesDto(String issueSummary,
                         String applicableLaw,
                         String legalExplanation,
                         List<String> relatedStatutes,
                         List<String> citedCases) {
        this.issueSummary = issueSummary != null ? issueSummary : "";
        this.applicableLaw = applicableLaw != null ? applicableLaw : "";
        this.legalExplanation = legalExplanation != null ? legalExplanation : "";
        this.relatedStatutes = relatedStatutes != null ? relatedStatutes : new ArrayList<>();
        this.citedCases = citedCases != null ? citedCases : new ArrayList<>();
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

    public List<String> getCitedCases() {
        return citedCases;
    }
}
