package lawSystem.ai;

import java.util.ArrayList;
import java.util.List;

import lawSystem.db.dao.PrecedentDAO;
import lawSystem.precedent.Precedent;
import lawSystem.precedent.PrecedentHit;
import lawSystem.precedent.PrecedentRagClient;

/**
 * 유사 판례 탐색 AI 기능이다.
 *
 * 기존의 더미 구현을 실제 RAG 파이프라인으로 교체했다.
 *   1. Python RAG 서비스에 사건 요약을 쿼리로 보내 유사 판례 후보(external_case_id + 점수)를 받는다.
 *   2. 각 후보를 precedent 테이블(단일 진실 공급원)에서 조회해 본문을 채운다.
 *   3. PrecedentAnalysisResult 로 변환해 반환한다.
 *
 * RAG 서비스가 꺼져 있거나 오류가 나면 빈 목록을 반환한다(graceful degradation).
 */
public class SimilarPrecedentsAnalysis extends AIAnalysisFunction {

    private String searchKeyword;
    private double similarityThreshold;
    private int maxResultCount;

    private final PrecedentRagClient ragClient;
    private final PrecedentDAO precedentDAO;

    /** 마지막 검색의 상세 결과(공식 execute 경로에서 1행 집계 후에도 상세 제공용). */
    private List<PrecedentAnalysisResult> lastResults = new ArrayList<>();

    public SimilarPrecedentsAnalysis() {
        this(new PrecedentRagClient(), new PrecedentDAO());
    }

    /** 테스트 등에서 의존성을 주입할 수 있는 생성자. */
    public SimilarPrecedentsAnalysis(PrecedentRagClient ragClient, PrecedentDAO precedentDAO) {
        super("ai-function-similar-precedents", "유사 판례 탐색 기능", "rag-v1", "ACTIVE");
        this.searchKeyword = "";
        this.similarityThreshold = 0.0;
        this.maxResultCount = 5;
        this.ragClient = ragClient;
        this.precedentDAO = precedentDAO;
    }

    public List<PrecedentAnalysisResult> searchSimilarPrecedents(
            String caseId,
            String summary
    ) {
        this.searchKeyword = summary;

        List<PrecedentAnalysisResult> results = new ArrayList<>();
        this.lastResults = results;

        if (summary == null || summary.trim().isEmpty()) {
            return results;
        }

        try {
            List<PrecedentHit> hits = ragClient.searchSimilarPrecedents(
                    summary, caseId, maxResultCount, similarityThreshold);

            int index = 0;
            for (PrecedentHit hit : hits) {
                Precedent precedent = precedentDAO.findByExternalId(hit.getCaseId());
                if (precedent == null) {
                    // 색인에는 있으나 DB 에 없는 경우(데이터 불일치) 건너뛴다.
                    System.err.println("[RAG] DB 에 없는 판례 external_case_id=" + hit.getCaseId());
                    continue;
                }
                results.add(toAnalysisResult(caseId, precedent, hit, index++));
            }
        } catch (Exception e) {
            // RAG 서비스 장애 시 기능 전체를 죽이지 않고 빈 결과를 반환한다.
            System.err.println("[RAG] 유사 판례 검색 실패 (빈 결과 반환): " + e.getMessage());
        }

        return results;
    }

    private PrecedentAnalysisResult toAnalysisResult(
            String caseId,
            Precedent precedent,
            PrecedentHit hit,
            int index
    ) {
        long stamp = System.nanoTime() + index;
        return new PrecedentAnalysisResult(
                "ai-result-" + stamp,
                "ai-request-" + stamp,
                caseId,
                "precedent-result-" + stamp,
                precedent.getPrecedentId(),
                precedent.getCaseName(),
                pickSummary(precedent),
                hit.getSimilarityScore(),
                trim(precedent.getIssues(), 1000)
        );
    }

    private String pickSummary(Precedent precedent) {
        String summary = precedent.getSummary();
        if (summary != null && !summary.trim().isEmpty()) {
            return trim(summary, 1000);
        }
        return trim(precedent.getIssues(), 1000);
    }

    private String trim(String text, int max) {
        if (text == null) {
            return null;
        }
        String t = text.trim();
        return t.length() <= max ? t : t.substring(0, max) + "...";
    }

    /**
     * 공식 경로(AIAnalysisFunction.execute → SIMILAR_PRECEDENTS)에서 호출된다.
     * request.getPrompt() 를 검색 쿼리로 사용하고 결과를 1행으로 집계한다(저장용).
     * 개별 판례 상세는 {@link #getLastResults()} 로 제공한다.
     */
    @Override
    public AIAnalysisResult findSimilarPrecedents(AIAnalysisRequest request) {
        String caseId = request.getTargetId();
        String query = request.getPrompt();
        List<PrecedentAnalysisResult> precedents = searchSimilarPrecedents(caseId, query);

        double confidence = precedents.isEmpty() ? 0.0 : precedents.get(0).getSimilarityScore();
        return createResult(request, renderForResult(precedents), confidence);
    }

    /** 마지막 searchSimilarPrecedents 호출의 상세 결과 목록. */
    public List<PrecedentAnalysisResult> getLastResults() {
        return lastResults;
    }

    private String renderForResult(List<PrecedentAnalysisResult> precedents) {
        if (precedents.isEmpty()) {
            return "유사 판례를 찾지 못했습니다.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("유사 판례 ").append(precedents.size()).append("건:\n");
        int i = 1;
        for (PrecedentAnalysisResult p : precedents) {
            sb.append(i++).append(". ")
              .append(p.getPrecedentTitle())
              .append(" (유사도 ")
              .append(String.format("%.4f", p.getSimilarityScore()))
              .append(")\n");
        }
        return sb.toString().trim();
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }

    public double getSimilarityThreshold() {
        return similarityThreshold;
    }

    public void setSimilarityThreshold(double similarityThreshold) {
        this.similarityThreshold = similarityThreshold;
    }

    public int getMaxResultCount() {
        return maxResultCount;
    }

    public void setMaxResultCount(int maxResultCount) {
        this.maxResultCount = maxResultCount;
    }
}
