package lawSystem.ai;

import java.util.ArrayList;
import java.util.List;

public class RecommendLawyers extends AIAnalysisFunction {
    private double similarityWeight;
    private double workloadWeight;
    private double specialtyWeight;

    public RecommendLawyers() {
        super("ai-function-recommend-lawyers", "사건 배당 변호사 추천 기능", "dummy-v1", "ACTIVE");
        this.similarityWeight = 0.5;
        this.workloadWeight = 0.3;
        this.specialtyWeight = 0.2;
    }

    public List<RecommendedLawyer> recommendLawyers(
            String caseId,
            List<String> lawyerIds
    ) {
        List<RecommendedLawyer> results = new ArrayList<>();

        if (caseId == null || caseId.trim().isEmpty()) {
            return results;
        }

        if (lawyerIds == null || lawyerIds.isEmpty()) {
            lawyerIds = new ArrayList<>();
            lawyerIds.add("associateLawyer001");
            lawyerIds.add("associateLawyer002");
        }

        for (String lawyerId : lawyerIds) {
            double similarityScore = 0.87;
            double workloadScore = calculateWorkloadScore(lawyerId);
            double specialtyScore = 0.82;

            results.add(new RecommendedLawyer(
                    "ai-result-" + System.currentTimeMillis(),
                    "ai-request-dummy-" + System.currentTimeMillis(),
                    caseId,
                    "recommendation-" + System.currentTimeMillis(),
                    lawyerId,
                    similarityScore,
                    workloadScore,
                    specialtyScore,
                    "유사 사건 경험과 업무량을 기준으로 추천된 더미 결과입니다."
            ));
        }

        return RecommendedLawyer.rankLawyers(results);
    }

    public double calculateWorkloadScore(String lawyerId) {
        if (lawyerId == null || lawyerId.trim().isEmpty()) {
            return 0.0;
        }

        return 0.75;
    }

    public double calculateTotalScore(
            double similarityScore,
            double workloadScore,
            double specialtyScore
    ) {
        return similarityScore * similarityWeight
                + workloadScore * workloadWeight
                + specialtyScore * specialtyWeight;
    }

    public double getSimilarityWeight() {
        return similarityWeight;
    }

    public double getWorkloadWeight() {
        return workloadWeight;
    }

    public double getSpecialtyWeight() {
        return specialtyWeight;
    }
}
