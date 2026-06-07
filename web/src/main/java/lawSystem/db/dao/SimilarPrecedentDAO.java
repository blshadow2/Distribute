package lawSystem.db.dao;

import lawSystem.db.DBA;
import lawSystem.legalCase.SimilarPrecedent;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * similar_precedent 테이블에 대한 DAO 이다.
 */
public class SimilarPrecedentDAO {

    public boolean insert(SimilarPrecedent precedent) {
        String sql = "INSERT INTO similar_precedent (precedent_id, case_id, precedent_title, court_name, " +
                "case_number, decision_date, precedent_summary, legal_issue, applied_legal_rule, " +
                "similarity_score, source_url, selected, registered_by, registered_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, precedent.getPrecedentId());
            pstmt.setString(2, precedent.getCaseId());
            pstmt.setString(3, precedent.getPrecedentTitle());
            pstmt.setString(4, precedent.getCourtName());
            pstmt.setString(5, precedent.getCaseNumber());
            pstmt.setDate(6, precedent.getDecisionDate() != null ? Date.valueOf(precedent.getDecisionDate()) : null);
            pstmt.setString(7, precedent.getPrecedentSummary());
            pstmt.setString(8, precedent.getLegalIssue());
            pstmt.setString(9, precedent.getAppliedLegalRule());
            pstmt.setDouble(10, precedent.getSimilarityScore());
            pstmt.setString(11, precedent.getSourceUrl());
            pstmt.setBoolean(12, precedent.isSelected());
            pstmt.setString(13, precedent.getRegisteredBy());

            LocalDateTime registeredAt = precedent.getRegisteredAt() != null
                    ? precedent.getRegisteredAt()
                    : LocalDateTime.now();
            pstmt.setTimestamp(14, Timestamp.valueOf(registeredAt));

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public SimilarPrecedent findById(String precedentId) {
        String sql = "SELECT * FROM similar_precedent WHERE precedent_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, precedentId);

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

    public List<SimilarPrecedent> findByCaseId(String caseId) {
        List<SimilarPrecedent> result = new ArrayList<>();
        String sql = "SELECT * FROM similar_precedent WHERE case_id = ? ORDER BY similarity_score DESC";

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

    public boolean update(SimilarPrecedent precedent) {
        String sql = "UPDATE similar_precedent SET precedent_title = ?, court_name = ?, case_number = ?, " +
                "decision_date = ?, precedent_summary = ?, legal_issue = ?, applied_legal_rule = ?, " +
                "similarity_score = ?, source_url = ?, selected = ? WHERE precedent_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, precedent.getPrecedentTitle());
            pstmt.setString(2, precedent.getCourtName());
            pstmt.setString(3, precedent.getCaseNumber());
            pstmt.setDate(4, precedent.getDecisionDate() != null ? Date.valueOf(precedent.getDecisionDate()) : null);
            pstmt.setString(5, precedent.getPrecedentSummary());
            pstmt.setString(6, precedent.getLegalIssue());
            pstmt.setString(7, precedent.getAppliedLegalRule());
            pstmt.setDouble(8, precedent.getSimilarityScore());
            pstmt.setString(9, precedent.getSourceUrl());
            pstmt.setBoolean(10, precedent.isSelected());
            pstmt.setString(11, precedent.getPrecedentId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(String precedentId) {
        String sql = "DELETE FROM similar_precedent WHERE precedent_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, precedentId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private SimilarPrecedent mapRow(ResultSet rs) throws SQLException {
        SimilarPrecedent precedent = new SimilarPrecedent(
                rs.getString("precedent_id"),
                rs.getString("case_id"),
                rs.getString("precedent_title"),
                rs.getString("case_number")
        );

        precedent.setCourtName(rs.getString("court_name"));
        Date decisionDate = rs.getDate("decision_date");
        if (decisionDate != null) {
            precedent.setDecisionDate(decisionDate.toLocalDate());
        }
        precedent.setPrecedentSummary(rs.getString("precedent_summary"));
        precedent.setLegalIssue(rs.getString("legal_issue"));
        precedent.setAppliedLegalRule(rs.getString("applied_legal_rule"));
        precedent.setSimilarityScore(rs.getDouble("similarity_score"));
        precedent.setSourceUrl(rs.getString("source_url"));
        precedent.setSelected(rs.getBoolean("selected"));
        precedent.setRegisteredBy(rs.getString("registered_by"));

        Timestamp registeredAt = rs.getTimestamp("registered_at");
        if (registeredAt != null) {
            precedent.setRegisteredAt(registeredAt.toLocalDateTime());
        }

        return precedent;
    }
}
