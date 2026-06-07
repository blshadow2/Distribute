package lawSystem.db.dao;

import lawSystem.db.DBA;
import lawSystem.member.Client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * client 테이블에 대한 DAO 이다.
 *
 * Client 는 Member 의 한 종류이므로, member 테이블의 행과
 * client 테이블의 행이 함께 생성되어야 한다.
 */
public class ClientDAO {

    private final MemberDAO memberDAO = new MemberDAO();

    /**
     * client 와 member 정보를 동시에 저장한다.
     */
    public boolean insert(Client client) {
        Connection connection = null;

        try {
            connection = DBA.getConnection();
            connection.setAutoCommit(false);

            // member 행을 같은 트랜잭션에서 저장한다.
            if (!memberDAO.insert(connection, client)) {
                connection.rollback();
                return false;
            }

            String sql = "INSERT INTO client (client_id, member_id, address, registered_case_count, identity_verified) " +
                    "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, client.getClientId());
                pstmt.setString(2, client.getMemberId());
                pstmt.setString(3, client.getAddress());
                pstmt.setInt(4, client.getRegisteredCaseCount());
                pstmt.setBoolean(5, client.isIdentityVerified());

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

    public Client findById(String clientId) {
        String sql = "SELECT c.client_id, c.member_id, c.address, c.registered_case_count, c.identity_verified, " +
                "m.name, m.email, m.password, m.phone_number " +
                "FROM client c JOIN member m ON c.member_id = m.member_id " +
                "WHERE c.client_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, clientId);

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

    public List<Client> findAll() {
        List<Client> result = new ArrayList<>();
        String sql = "SELECT c.client_id, c.member_id, c.address, c.registered_case_count, c.identity_verified, " +
                "m.name, m.email, m.password, m.phone_number " +
                "FROM client c JOIN member m ON c.member_id = m.member_id";

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

    public boolean update(Client client) {
        Connection connection = null;
        try {
            connection = DBA.getConnection();
            connection.setAutoCommit(false);

            if (!memberDAO.update(connection, client)) {
                connection.rollback();
                return false;
            }

            String sql = "UPDATE client SET address = ?, registered_case_count = ?, identity_verified = ? " +
                    "WHERE client_id = ?";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, client.getAddress());
                pstmt.setInt(2, client.getRegisteredCaseCount());
                pstmt.setBoolean(3, client.isIdentityVerified());
                pstmt.setString(4, client.getClientId());

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

    public boolean delete(String clientId) {
        // member 가 ON DELETE CASCADE 로 연결되어 있으므로, member 만 지우면
        // client 도 함께 삭제된다.
        String sql = "DELETE FROM member WHERE member_id = (SELECT member_id FROM client WHERE client_id = ?)";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, clientId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    static Client mapRow(ResultSet rs) throws SQLException {
        Client client = new Client(
                rs.getString("member_id"),
                rs.getString("client_id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("phone_number"),
                rs.getString("address")
        );

        client.setIdentityVerified(rs.getBoolean("identity_verified"));

        return client;
    }
}
