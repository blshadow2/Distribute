package lawSystem.db.dao;

import lawSystem.db.DBA;
import lawSystem.verification.VerificationResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * verification_result 테이블에 대한 DAO 이다.
 */
public class VerificationResultDAO {

    public boolean insert(VerificationResult result) {
        String sql = "INSERT INTO verification_result (verification_id, member_id, verification_method, " +
                "verified, verified_at, personal_identifier) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, result.getVerificationId());
            pstmt.setString(2, result.getMemberId());
            pstmt.setString(3, result.getVerificationMethod());
            pstmt.setBoolean(4, result.isVerified());
            pstmt.setTimestamp(5, result.getVerifiedAt() != null ? Timestamp.valueOf(result.getVerifiedAt()) : null);
            pstmt.setString(6, result.getPersonalIdentifier());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public VerificationResult findById(String verificationId) {
        String sql = "SELECT * FROM verification_result WHERE verification_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, verificationId);

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

    public List<VerificationResult> findByMemberId(String memberId) {
        List<VerificationResult> result = new ArrayList<>();
        String sql = "SELECT * FROM verification_result WHERE member_id = ? ORDER BY verified_at DESC";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, memberId);

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

    public boolean update(VerificationResult result) {
        String sql = "UPDATE verification_result SET verified = ?, verified_at = ?, personal_identifier = ? " +
                "WHERE verification_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setBoolean(1, result.isVerified());
            pstmt.setTimestamp(2, result.getVerifiedAt() != null ? Timestamp.valueOf(result.getVerifiedAt()) : null);
            pstmt.setString(3, result.getPersonalIdentifier());
            pstmt.setString(4, result.getVerificationId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(String verificationId) {
        String sql = "DELETE FROM verification_result WHERE verification_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, verificationId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private VerificationResult mapRow(ResultSet rs) throws SQLException {
        VerificationResult result = new VerificationResult(
                rs.getString("verification_id"),
                rs.getString("member_id"),
                rs.getString("verification_method")
        );

        result.setVerified(rs.getBoolean("verified"));
        Timestamp verifiedAt = rs.getTimestamp("verified_at");
        if (verifiedAt != null) {
            result.setVerifiedAt(verifiedAt.toLocalDateTime());
        }
        result.setPersonalIdentifier(rs.getString("personal_identifier"));

        return result;
    }
}
