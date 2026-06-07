package lawSystem.precedent;

import java.util.List;

import lawSystem.ai.PrecedentAnalysisResult;
import lawSystem.ai.SimilarPrecedentsAnalysis;

/**
 * Java ↔ Python RAG(:8000) ↔ MariaDB 연결만 격리해서 확인하는 1회성 테스트이다.
 *
 * 실행 전 준비:
 *   1. Python RAG 서비스가 떠 있어야 한다 (python -m scripts.serve)
 *   2. 시스템 환경변수 LAWSYSTEM_DB_* 가 잡혀 있어야 한다 (setx 후 새 창/IDE 재시작)
 *   3. precedent 테이블에 판례가 적재돼 있어야 한다
 */
public class RagConnectionTest {

    public static void main(String[] args) {
        // 임의의 사건 ID(참고용) + 검색 쿼리(사건 요약 대용)
        String caseId = "test-case-001";
        String summary = "공개된 개인정보를 정보주체의 동의 없이 수집·이용한 행위의 위법성";

        // 0) RAG 서비스 헬스 체크
        PrecedentRagClient client = new PrecedentRagClient();
        System.out.println("[1] RAG 헬스 체크: " + (client.isHealthy() ? "OK" : "실패(서비스 꺼짐?)"));

        // 1) 실제 기능 호출
        SimilarPrecedentsAnalysis ai = new SimilarPrecedentsAnalysis();
        List<PrecedentAnalysisResult> results = ai.searchSimilarPrecedents(caseId, summary);

        System.out.println("[2] 검색 쿼리: " + summary);
        System.out.println("[3] 결과 수: " + results.size());

        int i = 1;
        for (PrecedentAnalysisResult r : results) {
            System.out.println("------------------------------ #" + (i++));
            System.out.println("  판례ID  : " + r.getPrecedentId());
            System.out.println("  제목    : " + r.getPrecedentTitle());
            System.out.println("  유사도  : " + r.getSimilarityScore());
            System.out.println("  쟁점    : " + truncate(r.getLegalIssue(), 80));
            System.out.println("  요지    : " + truncate(r.getPrecedentSummary(), 120));
        }

        if (results.isEmpty()) {
            System.out.println("결과가 0건입니다. RAG 서비스/Threshold/DB 적재 상태를 확인하세요.");
        } else {
            System.out.println("==============================");
            System.out.println("연결 성공: RAG 검색 → DB 본문 조립까지 정상 동작했습니다.");
        }
    }

    private static String truncate(String s, int n) {
        if (s == null) {
            return "(없음)";
        }
        String t = s.replaceAll("\\s+", " ").trim();
        return t.length() <= n ? t : t.substring(0, n) + "...";
    }
}
