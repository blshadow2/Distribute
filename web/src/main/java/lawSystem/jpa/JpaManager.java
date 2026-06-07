package lawSystem.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * EntityManagerFactory / EntityManager 의 라이프사이클을 관리하는 JPA 진입점이다.
 *
 * <p>JDBC 기반 DBA 가 {@link java.sql.Connection} 을 다루는 역할을 했다면,
 * JpaManager 는 {@link EntityManager} 를 다룬다.</p>
 *
 * <h3>설정 우선순위</h3>
 * <ol>
 *   <li>환경 변수: LAWSYSTEM_DB_URL / LAWSYSTEM_DB_USER / LAWSYSTEM_DB_PASSWORD</li>
 *   <li>persistence.xml 의 기본값</li>
 * </ol>
 *
 * <h3>사용 예</h3>
 * <pre>
 *   JpaManager.execute(em -&gt; em.persist(client));
 *   Client found = JpaManager.query(em -&gt; em.find(Client.class, "client-1"));
 *   JpaManager.shutdown();
 * </pre>
 */
public final class JpaManager {

    private static final String PERSISTENCE_UNIT = "lawSystem";

    private static EntityManagerFactory factory;

    private JpaManager() {
        // 인스턴스 생성 방지
    }

    /**
     * EntityManagerFactory 를 생성한다. 이미 만들어진 경우는 재사용한다.
     */
    public static synchronized EntityManagerFactory factory() {
        if (factory != null && factory.isOpen()) {
            return factory;
        }

        Map<String, String> overrides = new HashMap<>();
        // URL 만 JPA 전용 (JPA_DB_URL), 없으면 LAWSYSTEM_DB_URL 로 fallback.
        // user/password 는 DAO 와 공유하므로 LAWSYSTEM_DB_USER/PASSWORD 만 사용.
        addIfPresent(overrides, "jakarta.persistence.jdbc.url",
                firstNonEmpty(System.getenv("JPA_DB_URL"), System.getenv("LAWSYSTEM_DB_URL")));
        addIfPresent(overrides, "jakarta.persistence.jdbc.user",     System.getenv("LAWSYSTEM_DB_USER"));
        addIfPresent(overrides, "jakarta.persistence.jdbc.password", System.getenv("LAWSYSTEM_DB_PASSWORD"));

        factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT, overrides);
        System.out.println("[JpaManager] EntityManagerFactory 생성 완료. PU = " + PERSISTENCE_UNIT);
        return factory;
    }

    private static String firstNonEmpty(String... values) {
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) {
                return v;
            }
        }
        return null;
    }

    private static void addIfPresent(Map<String, String> map, String key, String value) {
        if (value != null && !value.trim().isEmpty()) {
            map.put(key, value.trim());
        }
    }

    /**
     * EntityManager 를 열어 작업을 수행하고 닫는다. 트랜잭션 자동 관리.
     */
    public static void execute(Consumer<EntityManager> work) {
        EntityManager em = factory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            work.accept(em);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * 트랜잭션 안에서 값을 조회/계산해 반환한다. 읽기/쓰기 모두 가능.
     */
    public static <R> R query(Function<EntityManager, R> work) {
        EntityManager em = factory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            R result = work.apply(em);
            tx.commit();
            return result;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * 단발성 EntityManager 가 필요할 때 사용한다. 호출자가 close() 책임을 진다.
     * (Repository 내부에서 트랜잭션 경계를 직접 다룰 때만 사용)
     */
    public static EntityManager openEntityManager() {
        return factory().createEntityManager();
    }

    public static synchronized void shutdown() {
        if (factory != null && factory.isOpen()) {
            factory.close();
            System.out.println("[JpaManager] EntityManagerFactory 종료");
        }
        factory = null;
    }
}
