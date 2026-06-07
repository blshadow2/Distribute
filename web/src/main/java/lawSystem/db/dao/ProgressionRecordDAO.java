package lawSystem.db.dao;

import lawSystem.db.DBA;
import lawSystem.legalCase.ProgressionRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * progression_record 테이블에 대한 DAO 이다.
 */
public class ProgressionRecordDAO {

    public boolean insert(ProgressionRecord record) {
        String sql = "INSERT INTO progression_record (progress_id, case_id, writer_id, progress_status, " +
                "description, recent_action, requested_material) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, record.getProgressId());
            pstmt.setString(2, record.getCaseId());
            pstmt.setString(3, record.getWriterId());
            pstmt.setString(4, record.getProgressStatus());
            pstmt.setString(5, record.getDescription());
            pstmt.setString(6, record.getRecentAction());
            pstmt.setString(7, record.getRequestedMaterial());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ProgressionRecord findById(String progressId) {
        String sql = "SELECT * FROM progression_record WHERE progress_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, progressId);

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

    public List<ProgressionRecord> findByCaseId(String caseId) {
        List<ProgressionRecord> result = new ArrayList<>();
        String sql = "SELECT * FROM progression_record WHERE case_id = ?";

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

    public boolean update(ProgressionRecord record) {
        String sql = "UPDATE progression_record SET progress_status = ?, description = ?, recent_action = ?, " +
                "requested_material = ? WHERE progress_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, record.getProgressStatus());
            pstmt.setString(2, record.getDescription());
            pstmt.setString(3, record.getRecentAction());
            pstmt.setString(4, record.getRequestedMaterial());
            pstmt.setString(5, record.getProgressId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(String progressId) {
        String sql = "DELETE FROM progression_record WHERE progress_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, progressId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private ProgressionRecord mapRow(ResultSet rs) throws SQLException {
        ProgressionRecord record = new ProgressionRecord(
                rs.getString("progress_id"),
                rs.getString("case_id"),
                rs.getString("writer_id"),
                rs.getString("progress_status"),
                rs.getString("description"),
                rs.getString("recent_action")
        );

        record.setRequestedMaterial(rs.getString("requested_material"));
        return record;
    }
}
