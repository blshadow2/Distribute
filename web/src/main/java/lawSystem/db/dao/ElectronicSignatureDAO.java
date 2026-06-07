package lawSystem.db.dao;

import lawSystem.db.DBA;
import lawSystem.verification.ElectronicSignature;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * electronic_signature 테이블에 대한 DAO 이다.
 */
public class ElectronicSignatureDAO {

    public boolean insert(ElectronicSignature signature) {
        String sql = "INSERT INTO electronic_signature (signature_id, document_id, member_id, verification_id, " +
                "signed_at, signature_hash) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, signature.getSignatureId());
            pstmt.setString(2, signature.getDocumentId());
            pstmt.setString(3, signature.getMemberId());
            pstmt.setString(4, signature.getVerificationId());
            pstmt.setTimestamp(5, signature.getSignedAt() != null ? Timestamp.valueOf(signature.getSignedAt()) : null);
            pstmt.setString(6, signature.getSignatureHash());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ElectronicSignature findById(String signatureId) {
        String sql = "SELECT * FROM electronic_signature WHERE signature_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, signatureId);

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

    public List<ElectronicSignature> findByDocumentId(String documentId) {
        List<ElectronicSignature> result = new ArrayList<>();
        String sql = "SELECT * FROM electronic_signature WHERE document_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, documentId);

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

    public boolean delete(String signatureId) {
        String sql = "DELETE FROM electronic_signature WHERE signature_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, signatureId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private ElectronicSignature mapRow(ResultSet rs) throws SQLException {
        Timestamp signedAt = rs.getTimestamp("signed_at");

        return new ElectronicSignature(
                rs.getString("signature_id"),
                rs.getString("document_id"),
                rs.getString("member_id"),
                rs.getString("verification_id"),
                signedAt != null ? signedAt.toLocalDateTime() : null,
                rs.getString("signature_hash")
        );
    }
}
