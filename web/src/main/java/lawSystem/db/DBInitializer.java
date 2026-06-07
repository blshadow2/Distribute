package lawSystem.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DB 스키마 초기화 클래스이다.
 *
 * 1. CREATE DATABASE IF NOT EXISTS 로 데이터베이스를 생성한다.
 * 2. schema.sql 의 모든 CREATE TABLE 문을 실행하여 테이블을 구성한다.
 *
 * 시스템 최초 구동 시 한 번 호출하면 된다.
 */
public class DBInitializer {

    private static final String SCHEMA_PATH = "schema.sql";

    public static void initialize() {
        createDatabaseIfNotExists();
        createTablesIfNotExists();
    }

    private static void createDatabaseIfNotExists() {
        String databaseName = DBA.getDatabaseName();

        // 1) 이미 DB 가 존재하고 접속 권한이 있으면 그대로 사용한다.
        try (Connection connection = DBA.getConnection()) {
            System.out.println("[DBInitializer] 기존 데이터베이스에 접속 성공: " + databaseName);
            return;
        } catch (SQLException ignored) {
            // DB 가 없거나, 권한이 없는 경우. 다음 단계에서 생성을 시도한다.
        }

        // 2) 서버 단의 connection 으로 CREATE DATABASE 를 시도한다.
        //    이 단계는 사용자가 CREATE 권한을 가질 때만 성공한다.
        try (Connection connection = DBA.getServerConnection();
             Statement statement = connection.createStatement()) {

            String sql = "CREATE DATABASE IF NOT EXISTS " + databaseName
                    + " DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";

            statement.executeUpdate(sql);
            System.out.println("[DBInitializer] 데이터베이스 준비 완료: " + databaseName);

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "데이터베이스 생성 실패. 다음 중 하나를 확인하세요:\n"
                            + " - MariaDB 서버에 root 등 권한 있는 계정으로 접속해 "
                            + "`CREATE DATABASE " + databaseName + "; "
                            + "GRANT ALL ON " + databaseName + ".* TO '<유저>'@'%';` 를 실행했는지\n"
                            + " - db.properties 의 사용자/호스트가 위 GRANT 의 대상과 일치하는지", e);
        }
    }

    private static void createTablesIfNotExists() {
        List<String> statements = loadSchemaStatements();

        try (Connection connection = DBA.getConnection();
             Statement statement = connection.createStatement()) {

            for (String sql : statements) {
                statement.executeUpdate(sql);
            }

            System.out.println("[DBInitializer] 테이블 생성 완료. 실행된 SQL 개수: " + statements.size());

        } catch (SQLException e) {
            throw new IllegalStateException("테이블 생성 실패", e);
        }
    }

    private static List<String> loadSchemaStatements() {
        StringBuilder sb = new StringBuilder();

        try (InputStream input = DBInitializer.class.getClassLoader().getResourceAsStream(SCHEMA_PATH)) {
            if (input == null) {
                throw new IllegalStateException("schema.sql 파일을 찾을 수 없습니다: " + SCHEMA_PATH);
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(input, StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    // 한 줄 주석은 제거한다.
                    int commentIndex = line.indexOf("--");
                    if (commentIndex != -1) {
                        line = line.substring(0, commentIndex);
                    }
                    sb.append(line).append('\n');
                }
            }

        } catch (IOException e) {
            throw new IllegalStateException("schema.sql 로딩 실패", e);
        }

        return splitStatements(sb.toString());
    }

    private static List<String> splitStatements(String script) {
        List<String> result = new ArrayList<>();

        for (String raw : script.split(";")) {
            String trimmed = raw.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }

        return result;
    }

    /**
     * 개발 도중 테이블을 모두 비울 때 사용한다.
     * 운영 환경에서는 호출하지 않는다.
     */
    public static void dropAllTables() {
        String[] tables = {
                "precedent_keyword",
                "precedent",
                "electronic_signature",
                "verification_result",
                "ai_analysis_result",
                "ai_analysis_request",
                "ai_analysis_function",
                "retainer_condition",
                "retainer_request",
                "consultation_request",
                "consultation_schedule",
                "similar_precedent",
                "progression_record",
                "case_document",
                "evidence",
                "case_info_keyword",
                "case_info",
                "case_keyword",
                "legal_case",
                "staff",
                "associate_lawyer",
                "partner_lawyer",
                "lawyer_specialty",
                "lawyer",
                "client",
                "member"
        };

        try (Connection connection = DBA.getConnection();
             Statement statement = connection.createStatement()) {

            statement.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");

            for (String table : tables) {
                statement.executeUpdate("DROP TABLE IF EXISTS " + table);
            }

            statement.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");

            System.out.println("[DBInitializer] 전체 테이블 삭제 완료");

        } catch (SQLException e) {
            throw new IllegalStateException("테이블 삭제 실패", e);
        }
    }

    public static void main(String[] args) {
        // 단독 실행 시: 스키마를 새로 구성한다.
        initialize();
    }
}
