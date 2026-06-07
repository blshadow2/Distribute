package lawSystem.db.dao;

import lawSystem.db.DBA;
import lawSystem.retainer.ConditionStatus;
import lawSystem.retainer.RetainerCondition;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * retainer_condition 테이블에 대한 DAO 이다.
 */
public class RetainerConditionDAO {

    public boolean insert(RetainerCondition condition) {
        String sql = "INSERT INTO retainer_condition (condition_id, retainer_request_id, fee, scope, " +
                "additional_terms, revision_no, condition_status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, condition.getConditionId());
            pstmt.setString(2, condition.getRetainerRequestId());
            pstmt.setInt(3, condition.getFee());
            pstmt.setString(4, condition.getScope());
            pstmt.setString(5, condition.getAdditionalTerms());
            pstmt.setInt(6, condition.getRevisionNo());
            pstmt.setString(7, condition.getConditionStatus() != null ? condition.getConditionStatus().name() : null);

            LocalDateTime createdAt = condition.getCreatedAt() != null
                    ? condition.getCreatedAt()
                    : LocalDateTime.now();
            pstmt.setTimestamp(8, Timestamp.valueOf(createdAt));

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public RetainerCondition findById(String conditionId) {
        String sql = "SELECT * FROM retainer_condition WHERE condition_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, conditionId);

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

    public List<RetainerCondition> findByRequestId(String requestId) {
        List<RetainerCondition> result = new ArrayList<>();
        String sql = "SELECT * FROM retainer_condition WHERE retainer_request_id = ? ORDER BY revision_no";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, requestId);

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

    public boolean update(RetainerCondition condition) {
        String sql = "UPDATE retainer_condition SET fee = ?, scope = ?, additional_terms = ?, revision_no = ?, " +
                "condition_status = ? WHERE condition_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setInt(1, condition.getFee());
            pstmt.setString(2, condition.getScope());
            pstmt.setString(3, condition.getAdditionalTerms());
            pstmt.setInt(4, condition.getRevisionNo());
            pstmt.setString(5, condition.getConditionStatus() != null ? condition.getConditionStatus().name() : null);
            pstmt.setString(6, condition.getConditionId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(String conditionId) {
        String sql = "DELETE FROM retainer_condition WHERE condition_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, conditionId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private RetainerCondition mapRow(ResultSet rs) throws SQLException {
        RetainerCondition condition = new RetainerCondition(
                rs.getString("condition_id"),
                rs.getString("retainer_request_id"),
                rs.getInt("fee"),
                rs.getString("scope"),
                rs.getString("additional_terms")
        );

        condition.setRevisionNo(rs.getInt("revision_no"));

        String status = rs.getString("condition_status");
        if (status != null) {
            condition.setConditionStatus(ConditionStatus.valueOf(status));
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            condition.setCreatedAt(createdAt.toLocalDateTime());
        }

        return condition;
    }
}
