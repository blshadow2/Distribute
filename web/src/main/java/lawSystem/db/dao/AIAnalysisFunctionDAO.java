package lawSystem.db.dao;

import lawSystem.ai.AIAnalysisFunction;
import lawSystem.db.DBA;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * ai_analysis_function 테이블에 대한 DAO 이다.
 */
public class AIAnalysisFunctionDAO {

    public boolean insert(AIAnalysisFunction function) {
        String sql = "INSERT INTO ai_analysis_function (function_id, function_name, model_version, function_status) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, function.getFunctionId());
            pstmt.setString(2, function.getFunctionName());
            pstmt.setString(3, function.getModelVersion());
            pstmt.setString(4, function.getFunctionStatus());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public AIAnalysisFunction findById(String functionId) {
        String sql = "SELECT * FROM ai_analysis_function WHERE function_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, functionId);

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

    public List<AIAnalysisFunction> findAll() {
        List<AIAnalysisFunction> result = new ArrayList<>();
        String sql = "SELECT * FROM ai_analysis_function";

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

    public boolean delete(String functionId) {
        String sql = "DELETE FROM ai_analysis_function WHERE function_id = ?";

        try (Connection connection = DBA.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, functionId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private AIAnalysisFunction mapRow(ResultSet rs) throws SQLException {
        return new AIAnalysisFunction(
                rs.getString("function_id"),
                rs.getString("function_name"),
                rs.getString("model_version"),
                rs.getString("function_status")
        );
    }
}
