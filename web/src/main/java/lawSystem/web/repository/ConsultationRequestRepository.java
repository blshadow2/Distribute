package lawSystem.web.repository;

import java.util.List;

import lawSystem.consultation.ConsultationStatus;
import lawSystem.jpa.entity.ConsultationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultationRequestRepository extends JpaRepository<ConsultationRequest, String> {

    List<ConsultationRequest> findByClient_MemberIdOrderByRequestedAtDesc(String memberId);

    List<ConsultationRequest> findAllByOrderByRequestedAtDesc();

    /** 의뢰인이 특정 변호사와 특정 상태(예: COMPLETED)의 상담을 가지고 있는지. 수임 전제조건 검사용. */
    boolean existsByClient_MemberIdAndLawyer_MemberIdAndRequestStatus(
            String clientMemberId, String lawyerMemberId, ConsultationStatus requestStatus);
}
