package lawSystem.db.dao;

import lawSystem.db.DBA;
import lawSystem.legalCase.Case;
import lawSystem.legalCase.CaseCategory;
import lawSystem.legalCase.CaseStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * legal_case 테이블에 대한 DAO 이다.
 * 사건의 keywords 는 case_keyword 테이블에 별도로 저장한다.
 */
public class CaseDAO {

    public boolean insert(Case targetCase) {
        Connection connection = null;
        try {
            connection = DBA.getConnection();
            connection.setAutoCommit(false);

            String sql = "INSERT INTO legal_case (case_id, client_id, assigned_lawyer_id, title, category, " +
                    "current_stage, fact_description, case_status, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, targetCase.getCaseId());
                pstmt.setString(2, targetCase.getClientId());
                pstmt.setString(3, targetCase.getAssignedLawyerId());
                pstmt.setString(4, targetCase.getTitle());
                pstmt.setString(5, targetCase.getCategory() != null ? targetCase.getCategory().name() : null);
                pstmt.setString(6, targetCase.getCurrentStage());
                pstmt.setString(7, targetCase.getFactDescription());
                pstmt.setString(8, targetCase.getCaseStatus() != null ? targetCase.getCaseStatus().name() : null);

                LocalDateTime createdAt = targetCase.getCreatedAt();
                pstmt.setTimestamp(9,
                        createdAt != null ? Timestamp.valueOf(createdAt) : Timestamp.valueOf(LocalDateTime.now()));

                if (pstmt.executeUpdate() <= 0) {
                    connection.rollback();
                    return false;
                }
            }

            insertKeywords(connection, targetCase.getCaseId(), targetCase.getKeywords());

            connection.commit();
            return true;

        } catch (SQLException e) {
            try {
                if (connection != null) connection.rollback();
            } catch (SQLException ignored) {
            }
            e.printStackTrace();
            return false;
        } finally {
            DBA.close(connection);
        }
    }

    public Case findById(String caseId) {
        String sql = "SELECT * FROM legal_case WHERE case_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, caseId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Case found = mapRow(rs);
                    found.saveKeywords(findKeywords(connection, caseId));
                    return found;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Case> findByClientId(String clientId) {
        List<Case> result = new ArrayList<>();
        String sql = "SELECT * FROM legal_case WHERE client_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, clientId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Case found = mapRow(rs);
                    found.saveKeywords(findKeywords(connection, found.getCaseId()));
                    result.add(found);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public List<Case> findByAssignedLawyerId(String lawyerId) {
        List<Case> result = new ArrayList<>();
        String sql = "SELECT * FROM legal_case WHERE assigned_lawyer_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, lawyerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Case found = mapRow(rs);
                    found.saveKeywords(findKeywords(connection, found.getCaseId()));
                    result.add(found);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public List<Case> findAll() {
        List<Case> result = new ArrayList<>();
        String sql = "SELECT * FROM legal_case ORDER BY created_at DESC";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Case found = mapRow(rs);
                found.saveKeywords(findKeywords(connection, found.getCaseId()));
                result.add(found);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public boolean update(Case targetCase) {
        Connection connection = null;
        try {
            connection = DBA.getConnection();
            connection.setAutoCommit(false);

            String sql = "UPDATE legal_case SET client_id = ?, assigned_lawyer_id = ?, title = ?, " +
                    "category = ?, current_stage = ?, fact_description = ?, case_status = ? " +
                    "WHERE case_id = ?";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, targetCase.getClientId());
                pstmt.setString(2, targetCase.getAssignedLawyerId());
                pstmt.setString(3, targetCase.getTitle());
                pstmt.setString(4, targetCase.getCategory() != null ? targetCase.getCategory().name() : null);
                pstmt.setString(5, targetCase.getCurrentStage());
                pstmt.setString(6, targetCase.getFactDescription());
                pstmt.setString(7, targetCase.getCaseStatus() != null ? targetCase.getCaseStatus().name() : null);
                pstmt.setString(8, targetCase.getCaseId());

                if (pstmt.executeUpdate() <= 0) {
                    connection.rollback();
                    return false;
                }
            }

            // keywords 는 통째로 갈아 끼운다.
            deleteKeywords(connection, targetCase.getCaseId());
            insertKeywords(connection, targetCase.getCaseId(), targetCase.getKeywords());

            connection.commit();
            return true;

        } catch (SQLException e) {
            try {
                if (connection != null) connection.rollback();
            } catch (SQLException ignored) {
            }
            e.printStackTrace();
            return false;
        } finally {
            DBA.close(connection);
        }
    }

    public boolean updateStatus(String caseId, CaseStatus status) {
        String sql = "UPDATE legal_case SET case_status = ? WHERE case_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, status != null ? status.name() : null);
            pstmt.setString(2, caseId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(String caseId) {
        String sql = "DELETE FROM legal_case WHERE case_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, caseId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void insertKeywords(Connection connection, String caseId, List<String> keywords) throws SQLException {
        if (keywords == null || keywords.isEmpty()) {
            return;
        }

        String sql = "INSERT IGNORE INTO case_keyword (case_id, keyword) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (String keyword : keywords) {
                pstmt.setString(1, caseId);
                pstmt.setString(2, keyword);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    private void deleteKeywords(Connection connection, String caseId) throws SQLException {
        String sql = "DELETE FROM case_keyword WHERE case_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, caseId);
            pstmt.executeUpdate();
        }
    }

    private List<String> findKeywords(Connection connection, String caseId) throws SQLException {
        List<String> result = new ArrayList<>();
        String sql = "SELECT keyword FROM case_keyword WHERE case_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, caseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getString("keyword"));
                }
            }
        }

        return result;
    }

    private Case mapRow(ResultSet rs) throws SQLException {
        String categoryName = rs.getString("category");
        String statusName = rs.getString("case_status");

        Case found = new Case(
                rs.getString("case_id"),
                rs.getString("client_id"),
                rs.getString("title"),
                categoryName != null ? CaseCategory.valueOf(categoryName) : null,
                rs.getString("current_stage"),
                rs.getString("fact_description"),
                statusName != null ? CaseStatus.valueOf(statusName) : null
        );

        String assignedLawyerId = rs.getString("assigned_lawyer_id");
        if (assignedLawyerId != null) {
            found.setAssignedLawyerId(assignedLawyerId);
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            found.setCreatedAt(createdAt.toLocalDateTime());
        }

        return found;
    }
}
