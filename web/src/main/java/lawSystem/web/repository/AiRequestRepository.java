package lawSystem.web.repository;

import lawSystem.jpa.entity.AIAnalysisRequest;
import org.springframework.data.jpa.repository.JpaRepository;

/** ai_analysis_request 리포지토리 (JPA). */
public interface AiRequestRepository extends JpaRepository<AIAnalysisRequest, String> {
}
