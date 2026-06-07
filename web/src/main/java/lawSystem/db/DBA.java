package lawSystem.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Database Access (DBA)
 *
 * MariaDB와의 JDBC 연결을 관리하는 클래스이다.
 * - db.properties 파일에서 접속 정보를 로딩한다.
 * - DAO 계층은 이 클래스의 getConnection() 만을 통해 Connection을 얻는다.
 * - JDBC 자원(ResultSet, Statement, Connection)의 안전한 종료를 위한 close() 도구를 제공한다.
 */
public class DBA {

    private static final String PROPERTIES_PATH = "db.properties";

    private static String jdbcDriver;
    private static String jdbcUrl;
    private static String jdbcUsername;
    private static String jdbcPassword;
    private static String jdbcDatabase;

    private static boolean initialized = false;

    private DBA() {
        // 인스턴스 생성을 막는다.
    }

    /**
     * 우선순위에 따라 설정을 읽고 JDBC 드라이버를 로딩한다.
     *
     * 우선순위 (높은 → 낮은):
     *   1. 환경 변수: LAWSYSTEM_DB_URL / LAWSYSTEM_DB_USER / LAWSYSTEM_DB_PASSWORD / LAWSYSTEM_DB_DATABASE
     *   2. 클래스패스: lawSystem/db/db.properties (기본/fallback)
     *
     * 기기마다 다른 접속 정보를 쓰려면 환경 변수만 다르게 설정하면 된다.
     * 코드/공유 폴더의 db.properties 는 건드릴 필요 없다.
     */
    public static synchronized void init() {
        if (initialized) {
            return;
        }

        Properties properties = new Properties();

        // 1) 번들된 db.properties 를 fallback 으로 로딩
        try (InputStream input = DBA.class.getClassLoader().getResourceAsStream(PROPERTIES_PATH)) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            // 번들 파일 로딩 실패는 무시하고 환경 변수로만 진행 가능
            System.err.println("[DBA] db.properties 로딩 실패 (환경 변수만으로 진행): " + e.getMessage());
        }

        // 2) 환경 변수가 있으면 우선 사용 (없으면 properties 값 사용)
        jdbcDriver   = resolve("LAWSYSTEM_DB_DRIVER",   properties.getProperty("jdbc.driver"),
                                "org.mariadb.jdbc.Driver");
        jdbcUrl      = resolve("LAWSYSTEM_DB_URL",      properties.getProperty("jdbc.url"),      null);
        jdbcUsername = resolve("LAWSYSTEM_DB_USER",     properties.getProperty("jdbc.username"), null);
        jdbcPassword = resolve("LAWSYSTEM_DB_PASSWORD", properties.getProperty("jdbc.password"), null);
        jdbcDatabase = resolve("LAWSYSTEM_DB_DATABASE", properties.getProperty("jdbc.database"),
                                "law_system");

        if (jdbcUrl == null || jdbcUrl.trim().isEmpty()) {
            throw new IllegalStateException(
                    "JDBC URL 이 비어 있습니다. 환경 변수 LAWSYSTEM_DB_URL 또는 db.properties 의 jdbc.url 를 설정하세요."
            );
        }
        if (jdbcUsername == null) {
            throw new IllegalStateException(
                    "DB 사용자명이 비어 있습니다. 환경 변수 LAWSYSTEM_DB_USER 또는 db.properties 의 jdbc.username 을 설정하세요."
            );
        }
        if (jdbcPassword == null) {
            throw new IllegalStateException(
                    "DB 비밀번호가 비어 있습니다. 환경 변수 LAWSYSTEM_DB_PASSWORD 또는 db.properties 의 jdbc.password 를 설정하세요."
            );
        }

        try {
            Class.forName(jdbcDriver);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("JDBC 드라이버를 찾을 수 없습니다: " + jdbcDriver, e);
        }

        System.out.println("[DBA] 설정 로딩 완료. URL = " + jdbcUrl
                + " (user=" + jdbcUsername + ")");

        initialized = true;
    }

    /**
     * 환경 변수 → properties → 기본값 순으로 값을 찾고,
     * 양 끝 공백/탭/줄바꿈을 모두 제거하여 반환한다.
     *
     * 환경 변수에 실수로 들어간 leading TAB/space 가 JDBC URL 파싱을
     * 깨뜨리는 일을 막기 위함이다.
     */
    private static String resolve(String envName, String propertyValue, String defaultValue) {
        String fromEnv = System.getenv(envName);
        if (fromEnv != null && !fromEnv.trim().isEmpty()) {
            return fromEnv.trim();
        }
        if (propertyValue != null && !propertyValue.trim().isEmpty()) {
            return propertyValue.trim();
        }
        return defaultValue;
    }

    /**
     * MariaDB에 연결된 Connection 객체를 반환한다.
     * DAO 계층은 이 메서드만을 통해 DB에 접근해야 한다.
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            init();
        }

        return DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword);
    }

    /**
     * 데이터베이스 자체가 존재하지 않을 때 사용할 수 있는 서버 단의 Connection이다.
     * DBInitializer 가 CREATE DATABASE 를 수행할 때 사용한다.
     */
    public static Connection getServerConnection() throws SQLException {
        if (!initialized) {
            init();
        }

        String serverUrl = jdbcUrl.replace("/" + jdbcDatabase, "/");
        return DriverManager.getConnection(serverUrl, jdbcUsername, jdbcPassword);
    }

    public static String getDatabaseName() {
        if (!initialized) {
            init();
        }
        return jdbcDatabase;
    }

    public static void close(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException ignored) {
            }
        }
    }

    public static void close(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException ignored) {
            }
        }
    }

    public static void close(PreparedStatement preparedStatement) {
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException ignored) {
            }
        }
    }

    public static void close(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }

    public static void close(
            ResultSet resultSet,
            PreparedStatement preparedStatement,
            Connection connection
    ) {
        close(resultSet);
        close(preparedStatement);
        close(connection);
    }
}
