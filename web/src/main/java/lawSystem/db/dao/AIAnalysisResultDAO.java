package lawSystem.db.dao;

import lawSystem.ai.AIAnalysisResult;
import lawSystem.ai.AnalysisType;
import lawSystem.db.DBA;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ai_analysis_result 테이블에 대한 DAO 이다.
 */
public class AIAnalysisResultDAO {

    public boolean insert(AIAnalysisResult result) {
        String sql = "INSERT INTO ai_analysis_result (ai_result_id, ai_request_id, case_id, result_type, " +
                "summary_text, confidence_score, generated_at, reviewed, reviewer_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, result.getAiResultId());
            pstmt.setString(2, result.getAiRequestId());
            pstmt.setString(3, result.getCaseId());
            pstmt.setString(4, result.getResultType() != null ? result.getResultType().name() : null);
            pstmt.setString(5, result.getSummaryText());
            pstmt.setDouble(6, result.getConfidenceScore());

            LocalDateTime generatedAt = result.getGeneratedAt() != null
                    ? result.getGeneratedAt()
                    : LocalDateTime.now();
            pstmt.setTimestamp(7, Timestamp.valueOf(generatedAt));
            pstmt.setBoolean(8, result.isReviewed());
            pstmt.setString(9, result.getReviewerId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public AIAnalysisResult findById(String resultId) {
        String sql = "SELECT * FROM ai_analysis_result WHERE ai_result_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, resultId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public AIAnalysisResult findByRequestId(String requestId) {
        String sql = "SELECT * FROM ai_analysis_result WHERE ai_request_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, requestId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<AIAnalysisResult> findByCaseId(String caseId) {
        List<AIAnalysisResult> result = new ArrayList<>();
        String sql = "SELECT * FROM ai_analysis_result WHERE case_id = ? ORDER BY generated_at DESC";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, caseId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public boolean update(AIAnalysisResult result) {
        String sql = "UPDATE ai_analysis_result SET summary_text = ?, confidence_score = ?, reviewed = ?, " +
                "reviewer_id = ? WHERE ai_result_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, result.getSummaryText());
            pstmt.setDouble(2, result.getConfidenceScore());
            pstmt.setBoolean(3, result.isReviewed());
            pstmt.setString(4, result.getReviewerId());
            pstmt.setString(5, result.getAiResultId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(String resultId) {
        String sql = "DELETE FROM ai_analysis_result WHERE ai_result_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, resultId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private AIAnalysisResult mapRow(ResultSet rs) throws SQLException {
        String typeName = rs.getString("result_type");

        AIAnalysisResult result = new AIAnalysisResult(
                rs.getString("ai_result_id"),
                rs.getString("ai_request_id"),
                rs.getString("case_id"),
                typeName != null ? AnalysisType.valueOf(typeName) : null,
                rs.getString("summary_text"),
                rs.getDouble("confidence_score")
        );

        Timestamp generatedAt = rs.getTimestamp("generated_at");
        if (generatedAt != null) {
            result.setGeneratedAt(generatedAt.toLocalDateTime());
        }
        result.setReviewed(rs.getBoolean("reviewed"));
        result.setReviewerId(rs.getString("reviewer_id"));

        return result;
    }
}
