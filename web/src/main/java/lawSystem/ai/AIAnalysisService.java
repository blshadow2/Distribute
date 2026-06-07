package lawSystem.ai;

import lawSystem.db.dao.AIAnalysisRequestDAO;
import lawSystem.db.dao.AIAnalysisResultDAO;

/**
 * AI 분석의 설계 프로세스(분석 요청 → 분석 기능 호출 → 분석 결과 저장)를
 * 한 곳에서 수행하는 오케스트레이터이다.
 *
 *   ① AIAnalysisRequest 생성 + ai_analysis_request 저장 (FK 부모)
 *   ② request.sendToAIService(function) → function.execute(request) 로 실제 분석 수행
 *   ③ 요청 상태 갱신 + AIAnalysisResult 를 ai_analysis_result 에 저장
 *
 * 모든 AI 기능(요약/키워드/법리/유사판례 등)이 동일하게 이 경로를 사용한다.
 *
 * 주의(FK 전제):
 *   - requesterId 는 member 에 존재하거나 null
 *   - caseId 는 legal_case 에 존재하거나 null
 */
public class AIAnalysisService {

    private final AIAnalysisRequestDAO requestDAO;
    private final AIAnalysisResultDAO resultDAO;

    public AIAnalysisService() {
        this(new AIAnalysisRequestDAO(), new AIAnalysisResultDAO());
    }

    public AIAnalysisService(AIAnalysisRequestDAO requestDAO, AIAnalysisResultDAO resultDAO) {
        this.requestDAO = requestDAO;
        this.resultDAO = resultDAO;
    }

    /**
     * 분석을 수행하고 요청/결과를 모두 영속화한다.
     *
     * @param requesterId 요청자 member_id (없으면 null)
     * @param caseId      대상 사건 case_id (없으면 null)
     * @param type        분석 유형
     * @param prompt      기능에 전달할 입력(요약 대상 텍스트 / 검색 쿼리 등)
     * @param function    실제 분석을 수행할 AI 기능 객체
     * @return 저장된 결과(실패 시 null)
     */
    public AIAnalysisResult analyze(String requesterId,
                                    String caseId,
                                    AnalysisType type,
                                    String prompt,
                                    AIAnalysisFunction function) {
        // ① 분석 요청 생성 + 저장
        AIAnalysisRequest request =
                AIAnalysisRequest.createAIRequest(requesterId, "CASE", caseId, type, prompt);
        requestDAO.insert(request);

        // ② 분석 기능 호출 (sendToAIService 내부에서 상태 전이 + execute)
        AIAnalysisResult result = request.sendToAIService(function);

        // ③ 요청 상태 반영 + 결과 저장
        requestDAO.updateStatus(
                request.getAiAnalysisRequestId(),
                request.getRequestStatus(),
                request.getFailReason());

        if (result != null) {
            resultDAO.insert(result);
        }
        return result;
    }
}
