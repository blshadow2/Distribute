package lawSystem.db.dao;

import lawSystem.db.DBA;
import lawSystem.member.PartnerLawyer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * partner_lawyer 테이블에 대한 DAO 이다.
 * PartnerLawyer 저장 시 member, lawyer, partner_lawyer 세 테이블에 모두 행이 생성된다.
 */
public class PartnerLawyerDAO {

    private final LawyerDAO lawyerDAO = new LawyerDAO();

    public boolean insert(PartnerLawyer partnerLawyer) {
        Connection connection = null;
        try {
            connection = DBA.getConnection();
            connection.setAutoCommit(false);

            if (!lawyerDAO.insert(connection, partnerLawyer)) {
                connection.rollback();
                return false;
            }

            String sql = "INSERT INTO partner_lawyer (partner_lawyer_id, lawyer_id, managing_lawyer_id) " +
                    "VALUES (?, ?, ?)";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, "partner-" + partnerLawyer.getLawyerId());
                pstmt.setString(2, partnerLawyer.getLawyerId());
                pstmt.setString(3, partnerLawyer.getManagingLawyerId());

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

    public PartnerLawyer findByLawyerId(String lawyerId) {
        String sql = "SELECT pl.managing_lawyer_id, l.lawyer_id, l.member_id, l.license_number, " +
                "l.office_location, l.introduction, m.name, m.email, m.password, m.phone_number " +
                "FROM partner_lawyer pl " +
                "JOIN lawyer l ON pl.lawyer_id = l.lawyer_id " +
                "JOIN member m ON l.member_id = m.member_id " +
                "WHERE pl.lawyer_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, lawyerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    List<String> specialty = LawyerDAO.findSpecialties(connection, lawyerId);
                    return new PartnerLawyer(
                            rs.getString("member_id"),
                            rs.getString("lawyer_id"),
                            rs.getString("managing_lawyer_id"),
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

    public List<PartnerLawyer> findAll() {
        List<PartnerLawyer> result = new ArrayList<>();
        String sql = "SELECT lawyer_id FROM partner_lawyer";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                PartnerLawyer pl = findByLawyerId(rs.getString("lawyer_id"));
                if (pl != null) {
                    result.add(pl);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public boolean updateManagingLawyer(String lawyerId, String managingLawyerId) {
        String sql = "UPDATE partner_lawyer SET managing_lawyer_id = ? WHERE lawyer_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, managingLawyerId);
            pstmt.setString(2, lawyerId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(String lawyerId) {
        return lawyerDAO.delete(lawyerId);
    }
}
