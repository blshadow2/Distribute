package lawSystem.db.dao;

import lawSystem.db.DBA;
import lawSystem.legalCase.CaseDocument;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * case_document 테이블에 대한 DAO 이다.
 */
public class CaseDocumentDAO {

    public boolean insert(CaseDocument document) {
        String sql = "INSERT INTO case_document (document_id, case_id, document_type, title, content, " +
                "file_path, created_by, version, signed) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, document.getDocumentId());
            pstmt.setString(2, document.getCaseId());
            pstmt.setString(3, document.getDocumentType());
            pstmt.setString(4, document.getTitle());
            pstmt.setString(5, document.getContent());
            pstmt.setString(6, document.getFilePath());
            pstmt.setString(7, document.getCreatedBy());
            pstmt.setInt(8, document.getVersion());
            pstmt.setBoolean(9, document.isSigned());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public CaseDocument findById(String documentId) {
        String sql = "SELECT * FROM case_document WHERE document_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, documentId);

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

    public List<CaseDocument> findByCaseId(String caseId) {
        List<CaseDocument> result = new ArrayList<>();
        String sql = "SELECT * FROM case_document WHERE case_id = ?";

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

    public boolean update(CaseDocument document) {
        String sql = "UPDATE case_document SET document_type = ?, title = ?, content = ?, file_path = ?, " +
                "version = ?, signed = ? WHERE document_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, document.getDocumentType());
            pstmt.setString(2, document.getTitle());
            pstmt.setString(3, document.getContent());
            pstmt.setString(4, document.getFilePath());
            pstmt.setInt(5, document.getVersion());
            pstmt.setBoolean(6, document.isSigned());
            pstmt.setString(7, document.getDocumentId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean markSigned(String documentId) {
        String sql = "UPDATE case_document SET signed = TRUE WHERE document_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, documentId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(String documentId) {
        String sql = "DELETE FROM case_document WHERE document_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, documentId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private CaseDocument mapRow(ResultSet rs) throws SQLException {
        CaseDocument doc = new CaseDocument(
                rs.getString("document_id"),
                rs.getString("case_id"),
                rs.getString("document_type"),
                rs.getString("title"),
                rs.getString("content"),
                rs.getString("created_by")
        );

        doc.setFilePath(rs.getString("file_path"));
        doc.setVersion(rs.getInt("version"));
        doc.setSigned(rs.getBoolean("signed"));

        return doc;
    }
}
