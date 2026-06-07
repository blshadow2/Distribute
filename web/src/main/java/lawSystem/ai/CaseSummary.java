package lawSystem.ai;

import java.util.ArrayList;
import java.util.List;

import lawSystem.precedent.PrecedentRagClient;
import lawSystem.precedent.SummaryDto;

/**
 * 사건 내용 요약 AI 기능이다. (LLM 단순 호출)
 *
 * - 공식 경로: AIAnalysisFunction.execute → {@link #summarizeCase(AIAnalysisRequest)}
 * - 직접 호출: {@link #summarizeCase(String, String)} (Main 등)
 *
 * AI 서비스 장애 시 더미/빈 결과로 graceful degradation 한다.
 */
public class CaseSummary extends AIAnalysisFunction {

    private String summaryType;
    private boolean includeTimeline;
    private boolean includeMainIssues;

    private final PrecedentRagClient aiClient;

    public CaseSummary() {
        this(new PrecedentRagClient());
    }

    public CaseSummary(PrecedentRagClient aiClient) {
        super("ai-function-case-summary", "사건 내용 요약 기능", "llm-v1", "ACTIVE");
        this.summaryType = "BASIC";
        this.includeTimeline = true;
        this.includeMainIssues = true;
        this.aiClient = aiClient;
    }

    /** 공식 경로(execute) — request 의 진짜 ID 로 결과를 만들어 저장 가능하게 한다. */
    @Override
    public CaseAnalysisReport summarizeCase(AIAnalysisRequest request) {
        return buildReport(request.getTargetId(), request.getPrompt(),
                request.getAiAnalysisRequestId());
    }

    /** 직접 호출 — 사건 내용 텍스트로 바로 요약. */
    public CaseAnalysisReport summarizeCase(String caseId, String caseInfoText) {
        return buildReport(caseId, caseInfoText, "ai-request-direct-" + System.nanoTime());
    }

    private CaseAnalysisReport buildReport(String caseId, String caseInfoText, String aiRequestId) {
        // caseId 는 선택값이다. 웹에서 사건과 무관한 텍스트 요약도 허용한다.
        // (caseId 가 null 이면 결과의 case_id 도 null 로 저장됨 — FK 는 nullable)
        String summary;
        List<String> mainIssues;
        String timeline;
        double confidence;

        try {
            SummaryDto dto = aiClient.summarize(caseInfoText, caseId);
            summary = dto.getSummary();
            mainIssues = includeMainIssues ? dto.getMainIssues() : new ArrayList<>();
            timeline = includeTimeline ? dto.getTimeline() : "";
            confidence = (summary == null || summary.isEmpty()) ? 0.0 : 0.9;
            if (summary == null || summary.isEmpty()) {
                summary = "요약 결과가 비어 있습니다.";
            }
        } catch (Exception e) {
            System.err.println("[LLM] 사건 요약 실패(폴백 사용): " + e.getMessage());
            summary = "사건 요약을 생성하지 못했습니다(AI 서비스 오류).";
            mainIssues = includeMainIssues ? extractMainIssues(caseInfoText) : new ArrayList<>();
            timeline = includeTimeline ? createTimeline(caseId) : "";
            confidence = 0.0;
        }

        return new CaseAnalysisReport(
                "ai-result-" + System.nanoTime(),
                aiRequestId,
                caseId,
                "report-" + System.nanoTime(),
                summary,
                mainIssues,
                timeline,
                new ArrayList<>(),
                confidence);
    }

    /** AI 실패 시 폴백용 기본 타임라인. */
    public String createTimeline(String caseId) {
        return "사건 접수 → 사건 정보 입력 → 증거자료 등록 → 변호사 검토";
    }

    /** AI 실패 시 폴백용 기본 쟁점. */
    public List<String> extractMainIssues(String caseInfoText) {
        List<String> issues = new ArrayList<>();
        if (caseInfoText == null || caseInfoText.trim().isEmpty()) {
            issues.add("사건 정보 부족");
            return issues;
        }
        issues.add("사실관계 확인");
        issues.add("증거자료의 신빙성");
        issues.add("법적 책임 성립 여부");
        return issues;
    }

    public String getSummaryType() {
        return summaryType;
    }

    public boolean isIncludeTimeline() {
        return includeTimeline;
    }

    public boolean isIncludeMainIssues() {
        return includeMainIssues;
    }
}
