package lawSystem.db.dao;

import lawSystem.ai.AIAnalysisRequest;
import lawSystem.ai.AIRequestStatus;
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
 * ai_analysis_request 테이블에 대한 DAO 이다.
 */
public class AIAnalysisRequestDAO {

    public boolean insert(AIAnalysisRequest request) {
        String sql = "INSERT INTO ai_analysis_request (ai_analysis_request_id, requester_id, target_type, " +
                "target_id, analysis_type, prompt, request_status, requested_at, fail_reason) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, request.getAiAnalysisRequestId());
            pstmt.setString(2, request.getRequesterId());
            pstmt.setString(3, request.getTargetType());
            pstmt.setString(4, request.getTargetId());
            pstmt.setString(5, request.getAnalysisType() != null ? request.getAnalysisType().name() : null);
            pstmt.setString(6, request.getPrompt());
            pstmt.setString(7, request.getRequestStatus() != null ? request.getRequestStatus().name() : null);

            LocalDateTime requestedAt = request.getRequestedAt() != null
                    ? request.getRequestedAt()
                    : LocalDateTime.now();
            pstmt.setTimestamp(8, Timestamp.valueOf(requestedAt));
            pstmt.setString(9, request.getFailReason());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public AIAnalysisRequest findById(String requestId) {
        String sql = "SELECT * FROM ai_analysis_request WHERE ai_analysis_request_id = ?";

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

    public List<AIAnalysisRequest> findByRequesterId(String requesterId) {
        List<AIAnalysisRequest> result = new ArrayList<>();
        String sql = "SELECT * FROM ai_analysis_request WHERE requester_id = ? ORDER BY requested_at DESC";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, requesterId);

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

    public boolean updateStatus(String requestId, AIRequestStatus status, String failReason) {
        String sql = "UPDATE ai_analysis_request SET request_status = ?, fail_reason = ? " +
                "WHERE ai_analysis_request_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, status != null ? status.name() : null);
            pstmt.setString(2, failReason);
            pstmt.setString(3, requestId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(String requestId) {
        String sql = "DELETE FROM ai_analysis_request WHERE ai_analysis_request_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, requestId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private AIAnalysisRequest mapRow(ResultSet rs) throws SQLException {
        String typeName = rs.getString("analysis_type");

        AIAnalysisRequest request = new AIAnalysisRequest(
                rs.getString("ai_analysis_request_id"),
                rs.getString("requester_id"),
                rs.getString("target_type"),
                rs.getString("target_id"),
                typeName != null ? AnalysisType.valueOf(typeName) : null,
                rs.getString("prompt")
        );

        String status = rs.getString("request_status");
        if (status != null) {
            request.setRequestStatus(AIRequestStatus.valueOf(status));
        }

        Timestamp requestedAt = rs.getTimestamp("requested_at");
        if (requestedAt != null) {
            request.setRequestedAt(requestedAt.toLocalDateTime());
        }

        request.setFailReason(rs.getString("fail_reason"));

        return request;
    }
}
