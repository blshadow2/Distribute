package lawSystem.web.repository;

import java.util.List;

import lawSystem.jpa.entity.RetainerRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RetainerRequestRepository extends JpaRepository<RetainerRequest, String> {

    List<RetainerRequest> findByClient_MemberIdOrderByRequestedAtDesc(String memberId);

    List<RetainerRequest> findAllByOrderByRequestedAtDesc();
}
