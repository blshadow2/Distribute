package lawSystem.jpa.repository;

import lawSystem.jpa.JpaManager;
import lawSystem.jpa.entity.ConsultationRequest;

import java.util.List;

public class ConsultationRequestRepository extends BaseRepository<ConsultationRequest, String> {

    public ConsultationRequestRepository() {
        super(ConsultationRequest.class);
    }

    public List<ConsultationRequest> findByLawyer(String lawyerMemberId) {
        return JpaManager.query(em ->
                em.createQuery(
                        "SELECT r FROM ConsultationRequest r WHERE r.lawyer.memberId = :lawyerId ORDER BY r.requestedAt DESC",
                        ConsultationRequest.class
                ).setParameter("lawyerId", lawyerMemberId).getResultList()
        );
    }

    public List<ConsultationRequest> findByClient(String clientMemberId) {
        return JpaManager.query(em ->
                em.createQuery(
                        "SELECT r FROM ConsultationRequest r WHERE r.client.memberId = :clientId ORDER BY r.requestedAt DESC",
                        ConsultationRequest.class
                ).setParameter("clientId", clientMemberId).getResultList()
        );
    }
}
