package lawSystem.web.repository;

import java.util.List;

import lawSystem.jpa.entity.ConsultationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultationRequestRepository extends JpaRepository<ConsultationRequest, String> {

    List<ConsultationRequest> findByClient_MemberIdOrderByRequestedAtDesc(String memberId);

    List<ConsultationRequest> findAllByOrderByRequestedAtDesc();
}
