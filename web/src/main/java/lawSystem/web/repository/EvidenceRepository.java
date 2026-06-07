package lawSystem.web.repository;

import java.util.List;

import lawSystem.jpa.entity.Evidence;
import org.springframework.data.jpa.repository.JpaRepository;

/** 증거자료(evidence) 리포지토리. */
public interface EvidenceRepository extends JpaRepository<Evidence, String> {

    List<Evidence> findByLegalCase_CaseId(String caseId);
}
