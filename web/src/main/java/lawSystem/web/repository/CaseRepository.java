package lawSystem.web.repository;

import java.util.List;

import lawSystem.jpa.entity.LegalCase;
import lawSystem.legalCase.CaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 사건(legal_case) Spring Data JPA 리포지토리.
 */
public interface CaseRepository extends JpaRepository<LegalCase, String> {

    /** 특정 의뢰인의 사건 목록 (최신순). */
    List<LegalCase> findByClient_MemberIdOrderByCreatedAtDesc(String memberId);

    /** 특정 변호사에게 배당된(담당) 사건 목록 (최신순). */
    List<LegalCase> findByAssignedLawyer_MemberIdOrderByCreatedAtDesc(String memberId);

    /** 특정 상태의 사건 목록 (최신순) — 사건 배당 화면에서 RETAINED 조회용. */
    List<LegalCase> findByCaseStatusOrderByCreatedAtDesc(CaseStatus caseStatus);
}
