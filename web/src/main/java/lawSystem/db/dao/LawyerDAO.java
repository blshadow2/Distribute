package lawSystem.db.dao;

import lawSystem.db.DBA;
import lawSystem.member.Lawyer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * lawyer 테이블에 대한 DAO 이다.
 * specialty 는 List<String> 이므로 lawyer_specialty 테이블을 통해 관리한다.
 */
public class LawyerDAO {

    private final MemberDAO memberDAO = new MemberDAO();

    public boolean insert(Lawyer lawyer) {
        Connection connection = null;
        try {
            connection = DBA.getConnection();
            connection.setAutoCommit(false);

            if (!insert(connection, lawyer)) {
                connection.rollback();
                return false;
            }

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

    /**
     * 트랜잭션 공유용 오버로드.
     * PartnerLawyer/AssociateLawyer DAO 가 자신의 트랜잭션에서 호출한다.
     */
    public boolean insert(Connection connection, Lawyer lawyer) throws SQLException {
        if (!memberDAO.insert(connection, lawyer)) {
            return false;
        }

        String sql = "INSERT INTO lawyer (lawyer_id, member_id, license_number, office_location, " +
                "current_workload, introduction) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, lawyer.getLawyerId());
            pstmt.setString(2, lawyer.getMemberId());
            pstmt.setString(3, lawyer.getLicenseNumber());
            pstmt.setString(4, lawyer.getOfficeLocation());
            pstmt.setInt(5, lawyer.getCurrentWorkload());
            pstmt.setString(6, lawyer.getIntroduction());

            if (pstmt.executeUpdate() <= 0) {
                return false;
            }
        }

        insertSpecialties(connection, lawyer.getLawyerId(), lawyer.getSpecialty());
        return true;
    }

    public Lawyer findById(String lawyerId) {
        String sql = "SELECT l.lawyer_id, l.member_id, l.license_number, l.office_location, " +
                "l.current_workload, l.introduction, m.name, m.email, m.password, m.phone_number " +
                "FROM lawyer l JOIN member m ON l.member_id = m.member_id " +
                "WHERE l.lawyer_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, lawyerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    List<String> specialty = findSpecialties(connection, lawyerId);
                    return mapRow(rs, specialty);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Lawyer> findAll() {
        List<Lawyer> result = new ArrayList<>();
        String sql = "SELECT l.lawyer_id, l.member_id, l.license_number, l.office_location, " +
                "l.current_workload, l.introduction, m.name, m.email, m.password, m.phone_number " +
                "FROM lawyer l JOIN member m ON l.member_id = m.member_id";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String lawyerId = rs.getString("lawyer_id");
                List<String> specialty = findSpecialties(connection, lawyerId);
                result.add(mapRow(rs, specialty));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public boolean update(Lawyer lawyer) {
        Connection connection = null;
        try {
            connection = DBA.getConnection();
            connection.setAutoCommit(false);

            if (!memberDAO.update(connection, lawyer)) {
                connection.rollback();
                return false;
            }

            String sql = "UPDATE lawyer SET license_number = ?, office_location = ?, " +
                    "current_workload = ?, introduction = ? WHERE lawyer_id = ?";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, lawyer.getLicenseNumber());
                pstmt.setString(2, lawyer.getOfficeLocation());
                pstmt.setInt(3, lawyer.getCurrentWorkload());
                pstmt.setString(4, lawyer.getIntroduction());
                pstmt.setString(5, lawyer.getLawyerId());

                if (pstmt.executeUpdate() <= 0) {
                    connection.rollback();
                    return false;
                }
            }

            // specialty 는 전체 삭제 후 재삽입한다.
            deleteSpecialties(connection, lawyer.getLawyerId());
            insertSpecialties(connection, lawyer.getLawyerId(), lawyer.getSpecialty());

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

    public boolean delete(String lawyerId) {
        String sql = "DELETE FROM member WHERE member_id = (SELECT member_id FROM lawyer WHERE lawyer_id = ?)";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, lawyerId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void insertSpecialties(Connection connection, String lawyerId, List<String> specialty)
            throws SQLException {
        if (specialty == null || specialty.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO lawyer_specialty (lawyer_id, specialty) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (String s : specialty) {
                pstmt.setString(1, lawyerId);
                pstmt.setString(2, s);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    private void deleteSpecialties(Connection connection, String lawyerId) throws SQLException {
        String sql = "DELETE FROM lawyer_specialty WHERE lawyer_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, lawyerId);
            pstmt.executeUpdate();
        }
    }

    static List<String> findSpecialties(Connection connection, String lawyerId) throws SQLException {
        List<String> result = new ArrayList<>();
        String sql = "SELECT specialty FROM lawyer_specialty WHERE lawyer_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, lawyerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getString("specialty"));
                }
            }
        }

        return result;
    }

    static Lawyer mapRow(ResultSet rs, List<String> specialty) throws SQLException {
        return new Lawyer(
                rs.getString("member_id"),
                rs.getString("lawyer_id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("phone_number"),
                rs.getString("license_number"),
                rs.getString("office_location"),
                specialty,
                rs.getString("introduction")
        );
    }
}
