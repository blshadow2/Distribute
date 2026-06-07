package lawSystem.web.repository;

import lawSystem.jpa.entity.Lawyer;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 변호사 리포지토리. Lawyer 는 추상이라 Partner/Associate 가 함께 조회된다.
 * (검색 필터는 specialty 가 컬렉션이라 서비스에서 메모리 필터링한다 — 데이터가 적어 충분.)
 */
public interface LawyerRepository extends JpaRepository<Lawyer, String> {
}
