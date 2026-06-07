package lawSystem.db.dao;

import lawSystem.db.DBA;
import lawSystem.legalCase.CaseCategory;
import lawSystem.legalCase.CaseInfo;

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
 * case_info 테이블에 대한 DAO 이다.
 */
public class CaseInfoDAO {

    public boolean insert(CaseInfo caseInfo) {
        Connection connection = null;
        try {
            connection = DBA.getConnection();
            connection.setAutoCommit(false);

            String sql = "INSERT INTO case_info (case_info_id, client_id, case_id, title, category, " +
                    "current_stage, fact_description, incident_date, region, temporary_saved, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, caseInfo.getCaseInfoId());
                pstmt.setString(2, caseInfo.getClientId());
                pstmt.setString(3, caseInfo.getCaseId());
                pstmt.setString(4, caseInfo.getTitle());
                pstmt.setString(5, caseInfo.getCategory() != null ? caseInfo.getCategory().name() : null);
                pstmt.setString(6, caseInfo.getCurrentStage());
                pstmt.setString(7, caseInfo.getFactDescription());
                pstmt.setDate(8, caseInfo.getIncidentDate() != null ? Date.valueOf(caseInfo.getIncidentDate()) : null);
                pstmt.setString(9, caseInfo.getRegion());
                pstmt.setBoolean(10, caseInfo.isTemporarySaved());

                LocalDateTime createdAt = caseInfo.getCreatedAt() != null ? caseInfo.getCreatedAt() : LocalDateTime.now();
                LocalDateTime updatedAt = caseInfo.getUpdatedAt() != null ? caseInfo.getUpdatedAt() : LocalDateTime.now();
                pstmt.setTimestamp(11, Timestamp.valueOf(createdAt));
                pstmt.setTimestamp(12, Timestamp.valueOf(updatedAt));

                if (pstmt.executeUpdate() <= 0) {
                    connection.rollback();
                    return false;
                }
            }

            insertKeywords(connection, caseInfo.getCaseInfoId(), caseInfo.getKeywords());

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

    public CaseInfo findById(String caseInfoId) {
        String sql = "SELECT * FROM case_info WHERE case_info_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, caseInfoId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    CaseInfo info = mapRow(rs);
                    info.addKeywords(findKeywords(connection, caseInfoId));
                    return info;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<CaseInfo> findByClientId(String clientId) {
        List<CaseInfo> result = new ArrayList<>();
        String sql = "SELECT * FROM case_info WHERE client_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, clientId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    CaseInfo info = mapRow(rs);
                    info.addKeywords(findKeywords(connection, info.getCaseInfoId()));
                    result.add(info);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public boolean update(CaseInfo caseInfo) {
        Connection connection = null;
        try {
            connection = DBA.getConnection();
            connection.setAutoCommit(false);

            String sql = "UPDATE case_info SET case_id = ?, title = ?, category = ?, current_stage = ?, " +
                    "fact_description = ?, incident_date = ?, region = ?, temporary_saved = ?, updated_at = ? " +
                    "WHERE case_info_id = ?";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, caseInfo.getCaseId());
                pstmt.setString(2, caseInfo.getTitle());
                pstmt.setString(3, caseInfo.getCategory() != null ? caseInfo.getCategory().name() : null);
                pstmt.setString(4, caseInfo.getCurrentStage());
                pstmt.setString(5, caseInfo.getFactDescription());
                pstmt.setDate(6, caseInfo.getIncidentDate() != null ? Date.valueOf(caseInfo.getIncidentDate()) : null);
                pstmt.setString(7, caseInfo.getRegion());
                pstmt.setBoolean(8, caseInfo.isTemporarySaved());
                pstmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setString(10, caseInfo.getCaseInfoId());

                if (pstmt.executeUpdate() <= 0) {
                    connection.rollback();
                    return false;
                }
            }

            deleteKeywords(connection, caseInfo.getCaseInfoId());
            insertKeywords(connection, caseInfo.getCaseInfoId(), caseInfo.getKeywords());

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

    public boolean delete(String caseInfoId) {
        String sql = "DELETE FROM case_info WHERE case_info_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, caseInfoId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private CaseInfo mapRow(ResultSet rs) throws SQLException {
        String categoryName = rs.getString("category");

        CaseInfo info = new CaseInfo(
                rs.getString("case_info_id"),
                rs.getString("client_id"),
                rs.getString("title"),
                categoryName != null ? CaseCategory.valueOf(categoryName) : null,
                rs.getString("current_stage"),
                rs.getString("fact_description")
        );

        info.setCaseId(rs.getString("case_id"));
        Date incidentDate = rs.getDate("incident_date");
        if (incidentDate != null) {
            info.setIncidentDate(incidentDate.toLocalDate());
        }
        info.setRegion(rs.getString("region"));
        info.setTemporarySaved(rs.getBoolean("temporary_saved"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            info.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            info.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return info;
    }

    private void insertKeywords(Connection connection, String caseInfoId, List<String> keywords) throws SQLException {
        if (keywords == null || keywords.isEmpty()) return;

        String sql = "INSERT IGNORE INTO case_info_keyword (case_info_id, keyword) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (String keyword : keywords) {
                pstmt.setString(1, caseInfoId);
                pstmt.setString(2, keyword);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    private void deleteKeywords(Connection connection, String caseInfoId) throws SQLException {
        String sql = "DELETE FROM case_info_keyword WHERE case_info_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, caseInfoId);
            pstmt.executeUpdate();
        }
    }

    private List<String> findKeywords(Connection connection, String caseInfoId) throws SQLException {
        List<String> result = new ArrayList<>();
        String sql = "SELECT keyword FROM case_info_keyword WHERE case_info_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, caseInfoId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getString("keyword"));
                }
            }
        }
        return result;
    }
}
