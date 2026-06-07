package lawSystem.jpa.repository;

import lawSystem.jpa.JpaManager;
import lawSystem.jpa.entity.RetainerRequest;

import java.util.List;

public class RetainerRequestRepository extends BaseRepository<RetainerRequest, String> {

    public RetainerRequestRepository() {
        super(RetainerRequest.class);
    }

    public List<RetainerRequest> findByLawyer(String lawyerMemberId) {
        return JpaManager.query(em ->
                em.createQuery(
                        "SELECT r FROM RetainerRequest r WHERE r.lawyer.memberId = :lawyerId ORDER BY r.requestedAt DESC",
                        RetainerRequest.class
                ).setParameter("lawyerId", lawyerMemberId).getResultList()
        );
    }

    public List<RetainerRequest> findByClient(String clientMemberId) {
        return JpaManager.query(em ->
                em.createQuery(
                        "SELECT r FROM RetainerRequest r WHERE r.client.memberId = :clientId",
                        RetainerRequest.class
                ).setParameter("clientId", clientMemberId).getResultList()
        );
    }
}
