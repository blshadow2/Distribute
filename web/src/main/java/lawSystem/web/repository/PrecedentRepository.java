package lawSystem.web.repository;

import java.util.Optional;

import lawSystem.jpa.entity.Precedent;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 판례 마스터(precedent) Spring Data JPA 리포지토리.
 * RAG 검색 결과(external_case_id)로 본문을 역조회할 때도 사용한다.
 */
public interface PrecedentRepository extends JpaRepository<Precedent, String> {

    Optional<Precedent> findByExternalCaseId(String externalCaseId);
}
