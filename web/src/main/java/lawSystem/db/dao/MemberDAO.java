package lawSystem.db.dao;

import lawSystem.db.DBA;
import lawSystem.member.Member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * member 테이블에 대한 데이터 접근 객체이다.
 *
 * 변호사/의뢰인/사무직원의 공통 속성을 보관한다.
 * 하위 역할 정보(client, lawyer, staff)는 각 DAO 가 관리한다.
 */
public class MemberDAO {

    public boolean insert(Member member) {
        try (Connection connection = DBA.getConnection()) {
            return insert(connection, member);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 같은 트랜잭션으로 member 행을 함께 저장하기 위한 오버로드이다.
     * Client/Lawyer/Staff DAO 가 자신의 트랜잭션 안에서 호출한다.
     */
    public boolean insert(Connection connection, Member member) {
        String sql = "INSERT INTO member " +
                "(member_id, name, email, password, phone_number, role, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, member.getMemberId());
            pstmt.setString(2, member.getName());
            pstmt.setString(3, member.getEmail());
            pstmt.setString(4, member.getPassword());
            pstmt.setString(5, member.getPhoneNumber());
            pstmt.setString(6, member.getRole());

            LocalDateTime createdAt = member.getCreatedAt();
            pstmt.setTimestamp(7,
                    createdAt != null ? Timestamp.valueOf(createdAt) : Timestamp.valueOf(LocalDateTime.now()));

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Member findById(String memberId) {
        String sql = "SELECT * FROM member WHERE member_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, memberId);

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

    public Member findByEmail(String email) {
        String sql = "SELECT * FROM member WHERE email = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, email);

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

    public List<Member> findAll() {
        List<Member> result = new ArrayList<>();
        String sql = "SELECT * FROM member ORDER BY created_at";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                result.add(mapRow(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public boolean update(Member member) {
        try (Connection connection = DBA.getConnection()) {
            return update(connection, member);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Connection connection, Member member) {
        String sql = "UPDATE member SET name = ?, email = ?, password = ?, phone_number = ?, role = ? " +
                "WHERE member_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, member.getName());
            pstmt.setString(2, member.getEmail());
            pstmt.setString(3, member.getPassword());
            pstmt.setString(4, member.getPhoneNumber());
            pstmt.setString(5, member.getRole());
            pstmt.setString(6, member.getMemberId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(String memberId) {
        String sql = "DELETE FROM member WHERE member_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, memberId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    static Member mapRow(ResultSet rs) throws SQLException {
        Member member = new Member(
                rs.getString("member_id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("phone_number"),
                rs.getString("role")
        );

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            member.setCreatedAt(createdAt.toLocalDateTime());
        }

        return member;
    }
}
