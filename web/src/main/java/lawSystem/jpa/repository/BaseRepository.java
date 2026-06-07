package lawSystem.jpa.repository;

import jakarta.persistence.EntityManager;
import lawSystem.jpa.JpaManager;

import java.util.List;
import java.util.Optional;

/**
 * 모든 엔티티에 공통으로 적용되는 CRUD 기본 구현이다.
 *
 * <p>JPA Repository 패턴의 미니 버전이라고 보면 된다.
 * Spring Data JPA 가 인터페이스 만으로 자동 구현해 주는 부분을 직접 구현했다.</p>
 *
 * @param <T>  엔티티 타입
 * @param <ID> 식별자 타입 (대부분의 엔티티가 String 을 쓴다)
 */
public abstract class BaseRepository<T, ID> {

    protected final Class<T> entityClass;

    protected BaseRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * 새 엔티티 저장. 영속 컨텍스트에 등록되며 트랜잭션 커밋 시 INSERT 가 실행된다.
     */
    public T save(T entity) {
        return JpaManager.query(em -> {
            em.persist(entity);
            return entity;
        });
    }

    /**
     * 기존 엔티티 병합. 영속 상태 아닌 엔티티를 영속 컨텍스트에 합치고, 결과 인스턴스를 반환한다.
     */
    public T update(T entity) {
        return JpaManager.query(em -> em.merge(entity));
    }

    /**
     * PK 로 엔티티 조회. 없으면 Optional.empty.
     */
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(
                JpaManager.query(em -> em.find(entityClass, id))
        );
    }

    /**
     * 전체 조회. 작은 테이블에서만 사용.
     */
    public List<T> findAll() {
        return JpaManager.query(em ->
                em.createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e", entityClass)
                        .getResultList()
        );
    }

    /**
     * PK 로 삭제. 존재하지 않으면 아무 일도 하지 않는다.
     */
    public void deleteById(ID id) {
        JpaManager.execute(em -> {
            T managed = em.find(entityClass, id);
            if (managed != null) {
                em.remove(managed);
            }
        });
    }

    /**
     * JPQL 한 줄 조회 도우미.
     */
    protected List<T> queryList(String jpql, ParamSetter setter) {
        return JpaManager.query(em -> {
            var query = em.createQuery(jpql, entityClass);
            setter.set(em, query::setParameter);
            return query.getResultList();
        });
    }

    /**
     * 단건 조회 도우미. 결과 없으면 Optional.empty.
     */
    protected Optional<T> querySingle(String jpql, ParamSetter setter) {
        return JpaManager.query(em -> {
            var query = em.createQuery(jpql, entityClass);
            setter.set(em, query::setParameter);
            return query.getResultStream().findFirst();
        });
    }

    @FunctionalInterface
    protected interface ParamSetter {
        void set(EntityManager em, java.util.function.BiConsumer<String, Object> bind);
    }
}
