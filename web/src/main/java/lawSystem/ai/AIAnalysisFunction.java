package lawSystem.ai;

public class AIAnalysisFunction {
    private String functionId;
    private String functionName;
    private String modelVersion;
    private String functionStatus;

    public AIAnalysisFunction(
            String functionId,
            String functionName,
            String modelVersion,
            String functionStatus
    ) {
        this.functionId = functionId;
        this.functionName = functionName;
        this.modelVersion = modelVersion;
        this.functionStatus = functionStatus;
    }

    public AIAnalysisResult execute(AIAnalysisRequest request) {
        if (request == null) {
            return null;
        }

        switch (request.getAnalysisType()) {
            case CASE_SUMMARY:
                return summarizeCase(request);

            case DOCUMENT_DRAFT:
                return draftDocument(request);

            case LEGAL_RULE_ANALYSIS:
                return analyzeLegalIssue(request);

            case SIMILAR_PRECEDENTS:
                return findSimilarPrecedents(request);

            case LAWYER_RECOMMENDATION:
                return recommendLawyer(request);

            case KEYWORD_EXTRACTION:
                return extractKeywords(request);

            default:
                return null;
        }
    }

    public AIAnalysisResult summarizeCase(AIAnalysisRequest request) {
        String resultText =
                "사건 ID [" + request.getTargetId() + "]의 사실관계, 증거자료, 주요 쟁점을 요약했습니다.";

        return createResult(request, resultText, 0.85);
    }

    public AIAnalysisResult recommendLawyer(AIAnalysisRequest request) {
        String resultText =
                "사건 ID [" + request.getTargetId() + "]에 대해 유사 사건 경험과 업무량을 기준으로 변호사를 추천했습니다.";

        return createResult(request, resultText, 0.80);
    }

    public AIAnalysisResult analyzeLegalIssue(AIAnalysisRequest request) {
        String resultText =
                "문서 또는 사건 ID [" + request.getTargetId() + "]에 포함된 주요 법리와 관련 법률 조항을 분석했습니다.";

        return createResult(request, resultText, 0.82);
    }

    public AIAnalysisResult draftDocument(AIAnalysisRequest request) {
        String resultText =
                "사건 ID [" + request.getTargetId() + "]와 입력된 법리를 바탕으로 문서 초안을 작성했습니다.";

        return createResult(request, resultText, 0.78);
    }

    public AIAnalysisResult findSimilarPrecedents(AIAnalysisRequest request) {
        String resultText =
                "사건 ID [" + request.getTargetId() + "]와 유사한 판례 후보를 탐색했습니다.";

        return createResult(request, resultText, 0.76);
    }

    public AIAnalysisResult extractKeywords(AIAnalysisRequest request) {
        String resultText =
                "사건 ID [" + request.getTargetId() + "]에서 핵심 키워드를 추출했습니다.";

        return createResult(request, resultText, 0.88);
    }

    protected AIAnalysisResult createResult(
            AIAnalysisRequest request,
            String resultText,
            double confidenceScore
    ) {
        return new AIAnalysisResult(
                "ai-result-" + System.currentTimeMillis(),
                request.getAiAnalysisRequestId(),
                request.getTargetId(),
                request.getAnalysisType(),
                resultText,
                confidenceScore
        );
    }

    public String getFunctionId() {
        return functionId;
    }

    public String getFunctionName() {
        return functionName;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public String getFunctionStatus() {
        return functionStatus;
    }
}