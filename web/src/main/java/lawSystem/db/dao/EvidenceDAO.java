package lawSystem.db.dao;

import lawSystem.db.DBA;
import lawSystem.legalCase.Evidence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * evidence 테이블에 대한 DAO 이다.
 */
public class EvidenceDAO {

    public boolean insert(Evidence evidence) {
        String sql = "INSERT INTO evidence (evidence_id, case_id, file_name, file_type, file_path, description) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, evidence.getEvidenceId());
            pstmt.setString(2, evidence.getCaseId());
            pstmt.setString(3, evidence.getFileName());
            pstmt.setString(4, evidence.getFileType());
            pstmt.setString(5, evidence.getFilePath());
            pstmt.setString(6, evidence.getDescription());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Evidence findById(String evidenceId) {
        String sql = "SELECT * FROM evidence WHERE evidence_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, evidenceId);

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

    public List<Evidence> findByCaseId(String caseId) {
        List<Evidence> result = new ArrayList<>();
        String sql = "SELECT * FROM evidence WHERE case_id = ?";

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

    public boolean existsByFileNameInCase(String caseId, String fileName) {
        String sql = "SELECT 1 FROM evidence WHERE case_id = ? AND file_name = ? LIMIT 1";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, caseId);
            pstmt.setString(2, fileName);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Evidence evidence) {
        String sql = "UPDATE evidence SET file_name = ?, file_type = ?, file_path = ?, description = ? " +
                "WHERE evidence_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, evidence.getFileName());
            pstmt.setString(2, evidence.getFileType());
            pstmt.setString(3, evidence.getFilePath());
            pstmt.setString(4, evidence.getDescription());
            pstmt.setString(5, evidence.getEvidenceId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(String evidenceId) {
        String sql = "DELETE FROM evidence WHERE evidence_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, evidenceId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Evidence mapRow(ResultSet rs) throws SQLException {
        return new Evidence(
                rs.getString("evidence_id"),
                rs.getString("case_id"),
                rs.getString("file_name"),
                rs.getString("file_type"),
                rs.getString("file_path"),
                rs.getString("description")
        );
    }
}
