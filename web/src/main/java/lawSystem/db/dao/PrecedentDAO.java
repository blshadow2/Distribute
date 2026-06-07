package lawSystem.db.dao;

import lawSystem.db.DBA;
import lawSystem.precedent.Precedent;

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
 * precedent / precedent_keyword 테이블에 대한 DAO 이다.
 *
 * - {@link #upsert(Precedent)} : 적재(import) 시 동일 PK 면 갱신, 없으면 삽입한다.
 * - {@link #findByExternalId(String)} : RAG 검색 결과(external_case_id)로 본문을 역조회한다.
 */
public class PrecedentDAO {

    /**
     * 판례 한 건을 upsert 한다. precedent_id(PK) 가 같으면 모든 컬럼을 갱신한다.
     * 키워드는 매번 전량 삭제 후 재삽입한다.
     */
    public boolean upsert(Precedent precedent) {
        String sql = "INSERT INTO precedent (precedent_id, external_case_id, case_name, case_number, " +
                "court_name, court_type_code, decision_date, case_type, judgment_type, " +
                "issues, summary, referenced_statutes, referenced_cases, full_text, " +
                "domain, source, source_url, imported_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                // imported_at 은 최초 적재 시각을 보존하기 위해 UPDATE 절에서 제외한다.
                "ON DUPLICATE KEY UPDATE " +
                "external_case_id=VALUES(external_case_id), case_name=VALUES(case_name), " +
                "case_number=VALUES(case_number), court_name=VALUES(court_name), " +
                "court_type_code=VALUES(court_type_code), decision_date=VALUES(decision_date), " +
                "case_type=VALUES(case_type), judgment_type=VALUES(judgment_type), " +
                "issues=VALUES(issues), summary=VALUES(summary), " +
                "referenced_statutes=VALUES(referenced_statutes), referenced_cases=VALUES(referenced_cases), " +
                "full_text=VALUES(full_text), domain=VALUES(domain), " +
                "source=VALUES(source), source_url=VALUES(source_url)";

        try (Connection connection = DBA.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setString(1, precedent.getPrecedentId());
                    pstmt.setString(2, precedent.getExternalCaseId());
                    pstmt.setString(3, precedent.getCaseName());
                    pstmt.setString(4, precedent.getCaseNumber());
                    pstmt.setString(5, precedent.getCourtName());
                    pstmt.setString(6, precedent.getCourtTypeCode());
                    pstmt.setDate(7, precedent.getDecisionDate() != null
                            ? Date.valueOf(precedent.getDecisionDate()) : null);
                    pstmt.setString(8, precedent.getCaseType());
                    pstmt.setString(9, precedent.getJudgmentType());
                    pstmt.setString(10, precedent.getIssues());
                    pstmt.setString(11, precedent.getSummary());
                    pstmt.setString(12, precedent.getReferencedStatutes());
                    pstmt.setString(13, precedent.getReferencedCases());
                    pstmt.setString(14, precedent.getFullText());
                    pstmt.setString(15, precedent.getDomain());
                    pstmt.setString(16, precedent.getSource());
                    pstmt.setString(17, precedent.getSourceUrl());
                    LocalDateTime importedAt = precedent.getImportedAt() != null
                            ? precedent.getImportedAt() : LocalDateTime.now();
                    pstmt.setTimestamp(18, Timestamp.valueOf(importedAt));
                    pstmt.executeUpdate();
                }

                replaceKeywords(connection, precedent);

                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void replaceKeywords(Connection connection, Precedent precedent) throws SQLException {
        try (PreparedStatement del = connection.prepareStatement(
                "DELETE FROM precedent_keyword WHERE precedent_id = ?")) {
            del.setString(1, precedent.getPrecedentId());
            del.executeUpdate();
        }

        if (precedent.getKeywords() == null || precedent.getKeywords().isEmpty()) {
            return;
        }

        try (PreparedStatement ins = connection.prepareStatement(
                "INSERT IGNORE INTO precedent_keyword (precedent_id, keyword) VALUES (?, ?)")) {
            for (String keyword : precedent.getKeywords()) {
                if (keyword == null || keyword.trim().isEmpty()) {
                    continue;
                }
                ins.setString(1, precedent.getPrecedentId());
                ins.setString(2, keyword.trim());
                ins.addBatch();
            }
            ins.executeBatch();
        }
    }

    /**
     * RAG 가 반환한 external_case_id 로 판례 본문을 조회한다.
     * 검색 결과 → 본문 조립 시 사용하는 핵심 메서드이다.
     */
    public Precedent findByExternalId(String externalCaseId) {
        String sql = "SELECT * FROM precedent WHERE external_case_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, externalCaseId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Precedent precedent = mapRow(rs);
                    precedent.setKeywords(loadKeywords(connection, precedent.getPrecedentId()));
                    return precedent;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 이미 적재된 external_case_id 집합. 증분 적재 판단에 사용한다. */
    public List<String> findAllExternalIds() {
        List<String> ids = new ArrayList<>();
        String sql = "SELECT external_case_id FROM precedent";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                ids.add(rs.getString("external_case_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ids;
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM precedent";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0L;
    }

    private List<String> loadKeywords(Connection connection, String precedentId) throws SQLException {
        List<String> keywords = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT keyword FROM precedent_keyword WHERE precedent_id = ?")) {
            pstmt.setString(1, precedentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    keywords.add(rs.getString("keyword"));
                }
            }
        }
        return keywords;
    }

    private Precedent mapRow(ResultSet rs) throws SQLException {
        Precedent precedent = new Precedent(
                rs.getString("precedent_id"),
                rs.getString("external_case_id")
        );

        precedent.setCaseName(rs.getString("case_name"));
        precedent.setCaseNumber(rs.getString("case_number"));
        precedent.setCourtName(rs.getString("court_name"));
        precedent.setCourtTypeCode(rs.getString("court_type_code"));

        Date decisionDate = rs.getDate("decision_date");
        if (decisionDate != null) {
            precedent.setDecisionDate(decisionDate.toLocalDate());
        }

        precedent.setCaseType(rs.getString("case_type"));
        precedent.setJudgmentType(rs.getString("judgment_type"));
        precedent.setIssues(rs.getString("issues"));
        precedent.setSummary(rs.getString("summary"));
        precedent.setReferencedStatutes(rs.getString("referenced_statutes"));
        precedent.setReferencedCases(rs.getString("referenced_cases"));
        precedent.setFullText(rs.getString("full_text"));
        precedent.setDomain(rs.getString("domain"));
        precedent.setSource(rs.getString("source"));
        precedent.setSourceUrl(rs.getString("source_url"));

        Timestamp importedAt = rs.getTimestamp("imported_at");
        if (importedAt != null) {
            precedent.setImportedAt(importedAt.toLocalDateTime());
        }

        return precedent;
    }
}
