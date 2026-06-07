package lawSystem.ai;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RecommendedLawyer extends AIAnalysisResult {
    private String recommendationId;
    private String lawyerId;
    private double similarityScore;
    private double workloadScore;
    private double specialtyScore;
    private double totalScore;
    private String reason;

    public RecommendedLawyer(
            String aiResultId,
            String aiRequestId,
            String caseId,
            String recommendationId,
            String lawyerId,
            double similarityScore,
            double workloadScore,
            double specialtyScore,
            String reason
    ) {
        super(aiResultId, aiRequestId, caseId, AnalysisType.LAWYER_RECOMMENDATION, reason, 0.0);
        this.recommendationId = recommendationId;
        this.lawyerId = lawyerId;
        this.similarityScore = similarityScore;
        this.workloadScore = workloadScore;
        this.specialtyScore = specialtyScore;
        this.totalScore = calculateTotalScore();
        this.reason = reason;
        updateConfidenceScore(this.totalScore);
    }

    public double calculateTotalScore() {
        return similarityScore * 0.5
                + workloadScore * 0.3
                + specialtyScore * 0.2;
    }

    public String generateReason() {
        this.reason = "유사 사건 경험, 현재 업무량, 전문 분야를 종합하여 추천되었습니다.";
        updateResult(reason);
        return reason;
    }

    public static List<RecommendedLawyer> rankLawyers(List<RecommendedLawyer> results) {
        if (results == null) {
            return new ArrayList<>();
        }

        results.sort(Comparator.comparingDouble(RecommendedLawyer::getTotalScore).reversed());
        return results;
    }

    public String getRecommendationId() {
        return recommendationId;
    }

    public String getLawyerId() {
        return lawyerId;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    public double getWorkloadScore() {
        return workloadScore;
    }

    public double getSpecialtyScore() {
        return specialtyScore;
    }

    public double getTotalScore() {
        return totalScore;
    }

    public String getReason() {
        return reason;
    }
}