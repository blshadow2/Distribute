package lawSystem.db.dao;

import lawSystem.db.DBA;
import lawSystem.member.AssociateLawyer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * associate_lawyer 테이블에 대한 DAO 이다.
 */
public class AssociateLawyerDAO {

    private final LawyerDAO lawyerDAO = new LawyerDAO();

    public boolean insert(AssociateLawyer associateLawyer) {
        Connection connection = null;
        try {
            connection = DBA.getConnection();
            connection.setAutoCommit(false);

            if (!lawyerDAO.insert(connection, associateLawyer)) {
                connection.rollback();
                return false;
            }

            String sql = "INSERT INTO associate_lawyer (associate_lawyer_id, lawyer_id) VALUES (?, ?)";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, associateLawyer.getAssociateLawyerId());
                pstmt.setString(2, associateLawyer.getLawyerId());

                if (pstmt.executeUpdate() <= 0) {
                    connection.rollback();
                    return false;
                }
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

    public AssociateLawyer findByAssociateId(String associateLawyerId) {
        String sql = "SELECT al.associate_lawyer_id, l.lawyer_id, l.member_id, l.license_number, " +
                "l.office_location, l.introduction, m.name, m.email, m.password, m.phone_number " +
                "FROM associate_lawyer al " +
                "JOIN lawyer l ON al.lawyer_id = l.lawyer_id " +
                "JOIN member m ON l.member_id = m.member_id " +
                "WHERE al.associate_lawyer_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, associateLawyerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    List<String> specialty = LawyerDAO.findSpecialties(connection, rs.getString("lawyer_id"));
                    return new AssociateLawyer(
                            rs.getString("member_id"),
                            rs.getString("lawyer_id"),
                            rs.getString("associate_lawyer_id"),
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

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<AssociateLawyer> findAll() {
        List<AssociateLawyer> result = new ArrayList<>();
        String sql = "SELECT associate_lawyer_id FROM associate_lawyer";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                AssociateLawyer al = findByAssociateId(rs.getString("associate_lawyer_id"));
                if (al != null) {
                    result.add(al);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public boolean delete(String associateLawyerId) {
        String sql = "DELETE FROM member WHERE member_id = (" +
                "SELECT l.member_id FROM associate_lawyer al " +
                "JOIN lawyer l ON al.lawyer_id = l.lawyer_id " +
                "WHERE al.associate_lawyer_id = ?)";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, associateLawyerId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
