package lawSystem.db.dao;

import lawSystem.db.DBA;
import lawSystem.member.Staff;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * staff 테이블에 대한 DAO 이다.
 */
public class StaffDAO {

    private final MemberDAO memberDAO = new MemberDAO();

    public boolean insert(Staff staff) {
        Connection connection = null;
        try {
            connection = DBA.getConnection();
            connection.setAutoCommit(false);

            if (!memberDAO.insert(connection, staff)) {
                connection.rollback();
                return false;
            }

            String sql = "INSERT INTO staff (staff_id, member_id, department, position) VALUES (?, ?, ?, ?)";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, staff.getStaffId());
                pstmt.setString(2, staff.getMemberId());
                pstmt.setString(3, staff.getDepartment());
                pstmt.setString(4, staff.getPosition());

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

    public Staff findById(String staffId) {
        String sql = "SELECT s.staff_id, s.member_id, s.department, s.position, " +
                "m.name, m.email, m.password, m.phone_number " +
                "FROM staff s JOIN member m ON s.member_id = m.member_id " +
                "WHERE s.staff_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, staffId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Staff(
                            rs.getString("member_id"),
                            rs.getString("staff_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("phone_number"),
                            rs.getString("department"),
                            rs.getString("position")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Staff> findAll() {
        List<Staff> result = new ArrayList<>();
        String sql = "SELECT s.staff_id, s.member_id, s.department, s.position, " +
                "m.name, m.email, m.password, m.phone_number " +
                "FROM staff s JOIN member m ON s.member_id = m.member_id";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                result.add(new Staff(
                        rs.getString("member_id"),
                        rs.getString("staff_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("phone_number"),
                        rs.getString("department"),
                        rs.getString("position")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public boolean update(Staff staff) {
        Connection connection = null;
        try {
            connection = DBA.getConnection();
            connection.setAutoCommit(false);

            if (!memberDAO.update(connection, staff)) {
                connection.rollback();
                return false;
            }

            String sql = "UPDATE staff SET department = ?, position = ? WHERE staff_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, staff.getDepartment());
                pstmt.setString(2, staff.getPosition());
                pstmt.setString(3, staff.getStaffId());

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

    public boolean delete(String staffId) {
        String sql = "DELETE FROM member WHERE member_id = (SELECT member_id FROM staff WHERE staff_id = ?)";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, staffId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
