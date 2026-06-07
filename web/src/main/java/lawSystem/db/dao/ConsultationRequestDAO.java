package lawSystem.db.dao;

import lawSystem.consultation.ConsultationRequest;
import lawSystem.consultation.ConsultationStatus;
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
 * consultation_request 테이블에 대한 DAO 이다.
 */
public class ConsultationRequestDAO {

    public boolean insert(ConsultationRequest request) {
        String sql = "INSERT INTO consultation_request (consultation_request_id, case_id, client_id, lawyer_id, " +
                "schedule_id, request_status, request_memo, requested_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, request.getConsultationRequestId());
            pstmt.setString(2, request.getCaseId());
            pstmt.setString(3, request.getClientId());
            pstmt.setString(4, request.getLawyerId());
            pstmt.setString(5, request.getScheduleId());
            pstmt.setString(6, request.getRequestStatus() != null ? request.getRequestStatus().name() : null);
            pstmt.setString(7, request.getRequestMemo());

            LocalDateTime requestedAt = request.getRequestedAt() != null
                    ? request.getRequestedAt()
                    : LocalDateTime.now();
            pstmt.setTimestamp(8, Timestamp.valueOf(requestedAt));

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ConsultationRequest findById(String requestId) {
        String sql = "SELECT * FROM consultation_request WHERE consultation_request_id = ?";

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

    public List<ConsultationRequest> findByLawyerId(String lawyerId) {
        List<ConsultationRequest> result = new ArrayList<>();
        String sql = "SELECT * FROM consultation_request WHERE lawyer_id = ? ORDER BY requested_at DESC";

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

    public List<ConsultationRequest> findByClientId(String clientId) {
        List<ConsultationRequest> result = new ArrayList<>();
        String sql = "SELECT * FROM consultation_request WHERE client_id = ? ORDER BY requested_at DESC";

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

    public boolean updateStatus(String requestId, ConsultationStatus status) {
        String sql = "UPDATE consultation_request SET request_status = ? WHERE consultation_request_id = ?";

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

    public boolean updateSchedule(String requestId, String scheduleId) {
        String sql = "UPDATE consultation_request SET schedule_id = ?, request_status = 'SCHEDULE_CHANGED' " +
                "WHERE consultation_request_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, scheduleId);
            pstmt.setString(2, requestId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(String requestId) {
        String sql = "DELETE FROM consultation_request WHERE consultation_request_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, requestId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private ConsultationRequest mapRow(ResultSet rs) throws SQLException {
        ConsultationRequest request = new ConsultationRequest(
                rs.getString("consultation_request_id"),
                rs.getString("case_id"),
                rs.getString("client_id"),
                rs.getString("lawyer_id"),
                rs.getString("schedule_id"),
                rs.getString("request_memo")
        );

        String status = rs.getString("request_status");
        if (status != null) {
            request.setRequestStatus(ConsultationStatus.valueOf(status));
        }

        Timestamp requestedAt = rs.getTimestamp("requested_at");
        if (requestedAt != null) {
            request.setRequestedAt(requestedAt.toLocalDateTime());
        }

        return request;
    }
}
