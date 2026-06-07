package lawSystem.db.dao;

import lawSystem.consultation.ConsultationSchedule;
import lawSystem.consultation.ScheduleStatus;
import lawSystem.db.DBA;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * consultation_schedule 테이블에 대한 DAO 이다.
 */
public class ConsultationScheduleDAO {

    public boolean insert(ConsultationSchedule schedule) {
        String sql = "INSERT INTO consultation_schedule (schedule_id, lawyer_id, date_time, duration, available_status) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, schedule.getScheduleId());
            pstmt.setString(2, schedule.getLawyerId());
            pstmt.setTimestamp(3, schedule.getDateTime() != null ? Timestamp.valueOf(schedule.getDateTime()) : null);
            pstmt.setInt(4, schedule.getDuration());
            pstmt.setString(5, schedule.getAvailableStatus() != null ? schedule.getAvailableStatus().name() : null);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ConsultationSchedule findById(String scheduleId) {
        String sql = "SELECT * FROM consultation_schedule WHERE schedule_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, scheduleId);

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

    public List<ConsultationSchedule> findByLawyerId(String lawyerId) {
        List<ConsultationSchedule> result = new ArrayList<>();
        String sql = "SELECT * FROM consultation_schedule WHERE lawyer_id = ? ORDER BY date_time";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, lawyerId);

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

    public List<ConsultationSchedule> findAvailableByLawyerId(String lawyerId) {
        List<ConsultationSchedule> result = new ArrayList<>();
        String sql = "SELECT * FROM consultation_schedule WHERE lawyer_id = ? AND available_status = 'AVAILABLE' " +
                "ORDER BY date_time";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, lawyerId);

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

    public boolean update(ConsultationSchedule schedule) {
        String sql = "UPDATE consultation_schedule SET date_time = ?, duration = ?, available_status = ? " +
                "WHERE schedule_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setTimestamp(1, schedule.getDateTime() != null ? Timestamp.valueOf(schedule.getDateTime()) : null);
            pstmt.setInt(2, schedule.getDuration());
            pstmt.setString(3, schedule.getAvailableStatus() != null ? schedule.getAvailableStatus().name() : null);
            pstmt.setString(4, schedule.getScheduleId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(String scheduleId) {
        String sql = "DELETE FROM consultation_schedule WHERE schedule_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, scheduleId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private ConsultationSchedule mapRow(ResultSet rs) throws SQLException {
        Timestamp dateTime = rs.getTimestamp("date_time");

        ConsultationSchedule schedule = new ConsultationSchedule(
                rs.getString("schedule_id"),
                rs.getString("lawyer_id"),
                dateTime != null ? dateTime.toLocalDateTime() : null,
                rs.getInt("duration")
        );

        String status = rs.getString("available_status");
        if (status != null) {
            schedule.setAvailableStatus(ScheduleStatus.valueOf(status));
        }

        return schedule;
    }
}
