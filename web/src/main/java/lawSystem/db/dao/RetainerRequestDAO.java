package lawSystem.db.dao;

import lawSystem.db.DBA;
import lawSystem.retainer.RetainerRequest;
import lawSystem.retainer.RetainerStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * retainer_request 테이블에 대한 DAO 이다.
 */
public class RetainerRequestDAO {

    public boolean insert(RetainerRequest request) {
        String sql = "INSERT INTO retainer_request (retainer_request_id, case_id, client_id, lawyer_id, " +
                "request_content, desired_scope, desired_fee, desired_result, request_status, requested_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, request.getRetainerRequestId());
            pstmt.setString(2, request.getCaseId());
            pstmt.setString(3, request.getClientId());
            pstmt.setString(4, request.getLawyerId());
            pstmt.setString(5, request.getRequestContent());
            pstmt.setString(6, request.getDesiredScope());
            pstmt.setInt(7, request.getDesiredFee());
            pstmt.setString(8, request.getDesiredResult());
            pstmt.setString(9, request.getRequestStatus() != null ? request.getRequestStatus().name() : null);

            LocalDateTime requestedAt = request.getRequestedAt() != null
                    ? request.getRequestedAt()
                    : LocalDateTime.now();
            pstmt.setTimestamp(10, Timestamp.valueOf(requestedAt));

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public RetainerRequest findById(String requestId) {
        String sql = "SELECT * FROM retainer_request WHERE retainer_request_id = ?";

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

    public List<RetainerRequest> findByLawyerId(String lawyerId) {
        List<RetainerRequest> result = new ArrayList<>();
        String sql = "SELECT * FROM retainer_request WHERE lawyer_id = ? ORDER BY requested_at DESC";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, lawyerId);

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

    public List<RetainerRequest> findByClientId(String clientId) {
        List<RetainerRequest> result = new ArrayList<>();
        String sql = "SELECT * FROM retainer_request WHERE client_id = ? ORDER BY requested_at DESC";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, clientId);

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

    public boolean updateStatus(String requestId, RetainerStatus status) {
        String sql = "UPDATE retainer_request SET request_status = ? WHERE retainer_request_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, status != null ? status.name() : null);
            pstmt.setString(2, requestId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(String requestId) {
        String sql = "DELETE FROM retainer_request WHERE retainer_request_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, requestId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private RetainerRequest mapRow(ResultSet rs) throws SQLException {
        RetainerRequest request = new RetainerRequest(
                rs.getString("retainer_request_id"),
                rs.getString("case_id"),
                rs.getString("client_id"),
                rs.getString("lawyer_id"),
                rs.getString("request_content"),
                rs.getString("desired_scope"),
                rs.getInt("desired_fee"),
                rs.getString("desired_result")
        );

        String status = rs.getString("request_status");
        if (status != null) {
            request.updateRequestStatus(RetainerStatus.valueOf(status));
        }

        Timestamp requestedAt = rs.getTimestamp("requested_at");
        if (requestedAt != null) {
            request.setRequestedAt(requestedAt.toLocalDateTime());
        }

        return request;
    }
}
