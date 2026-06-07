package lawSystem.precedent;

import java.util.List;

import lawSystem.ai.AIAnalysisResult;
import lawSystem.ai.AIAnalysisService;
import lawSystem.ai.AnalysisType;
import lawSystem.ai.CaseSummary;

/**
 * Phase 3 Step 2 검증용 1회성 테스트이다.
 *
 * (A) RAG/LLM 서비스 3종(요약/키워드/법리)이 Java 클라이언트로 정상 호출되는지 확인.
 * (B) (선택) 실제 legal_case 에 존재하는 caseId 를 인자로 주면,
 *     AIAnalysisService 로 "요청→호출→저장" 영속화까지 검증한다.
 *
 * 실행 전: Python RAG 서비스(:8000) + Ollama 기동 필요.
 *   - 영속화 검증(B)을 하려면 DB 환경변수(LAWSYSTEM_DB_*)와
 *     legal_case 에 존재하는 case_id 인자가 필요하다.
 */
public class Step2Test {

    public static void main(String[] args) {
        PrecedentRagClient client = new PrecedentRagClient();
        System.out.println("[0] RAG 헬스 체크: " + (client.isHealthy() ? "OK" : "실패(서비스 꺼짐?)"));

        String text = "피고는 정보주체의 동의 없이 공개된 개인정보를 수집하여 제3자에게 제공하였다. "
                + "원고는 개인정보자기결정권 침해를 이유로 손해배상을 청구하였다.";

        // (A) 3종 기능 직접 호출
        System.out.println("\n===== [1] 내용 요약 (/llm/summarize) =====");
        SummaryDto s = client.summarize(text, null);
        System.out.println("요약    : " + s.getSummary());
        System.out.println("주요쟁점: " + s.getMainIssues());
        System.out.println("타임라인: " + s.getTimeline());

        System.out.println("\n===== [2] 키워드 추출 (/llm/extract-keywords) =====");
        List<String> keywords = client.extractKeywords(text, 5);
        System.out.println("키워드  : " + keywords);

        System.out.println("\n===== [3] 법리 설명 (RAG 결합, /rag/analyze-rules) =====");
        LegalRulesDto r = client.analyzeLegalRules(text, null, 5);
        System.out.println("쟁점요약: " + r.getIssueSummary());
        System.out.println("적용법리: " + r.getApplicableLaw());
        System.out.println("설명    : " + r.getLegalExplanation());
        System.out.println("관련법령: " + r.getRelatedStatutes());
        System.out.println("인용판례: " + r.getCitedCases());

        // (B) 영속화 경로 (선택): args[0] = legal_case 에 존재하는 case_id
        if (args.length > 0) {
            String caseId = args[0];
            System.out.println("\n===== [4] 영속화 검증 (AIAnalysisService) caseId=" + caseId + " =====");
            AIAnalysisService service = new AIAnalysisService();
            AIAnalysisResult saved = service.analyze(
                    null,                       // requesterId (null 허용)
                    caseId,                     // 실제 legal_case 의 case_id 여야 FK 만족
                    AnalysisType.CASE_SUMMARY,
                    text,
                    new CaseSummary());
            if (saved != null) {
                System.out.println("저장된 결과 ID: " + saved.getAiResultId());
                System.out.println("→ ai_analysis_request / ai_analysis_result 테이블을 확인하세요.");
            } else {
                System.out.println("저장 실패(결과 null). FK(case_id) 또는 RAG 상태를 확인하세요.");
            }
        } else {
            System.out.println("\n(영속화 검증은 생략: 실제 case_id 를 인자로 주면 [4]가 실행됩니다.)");
        }

        System.out.println("\n==============================");
        System.out.println("Step 2 기능 확인 완료.");
    }
}
