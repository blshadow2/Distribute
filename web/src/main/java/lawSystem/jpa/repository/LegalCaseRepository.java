package lawSystem.jpa.repository;

import lawSystem.jpa.JpaManager;
import lawSystem.jpa.entity.LegalCase;
import lawSystem.legalCase.CaseStatus;

import java.util.List;

public class LegalCaseRepository extends BaseRepository<LegalCase, String> {

    public LegalCaseRepository() {
        super(LegalCase.class);
    }

    public List<LegalCase> findByClient(String clientMemberId) {
        return JpaManager.query(em ->
                em.createQuery(
                        "SELECT c FROM LegalCase c WHERE c.client.memberId = :clientId ORDER BY c.createdAt DESC",
                        LegalCase.class
                ).setParameter("clientId", clientMemberId).getResultList()
        );
    }

    public List<LegalCase> findByAssignedLawyer(String lawyerMemberId) {
        return JpaManager.query(em ->
                em.createQuery(
                        "SELECT c FROM LegalCase c WHERE c.assignedLawyer.memberId = :lawyerId",
                        LegalCase.class
                ).setParameter("lawyerId", lawyerMemberId).getResultList()
        );
    }

    public List<LegalCase> findByStatus(CaseStatus status) {
        return JpaManager.query(em ->
                em.createQuery(
                        "SELECT c FROM LegalCase c WHERE c.caseStatus = :status",
                        LegalCase.class
                ).setParameter("status", status).getResultList()
        );
    }
}
