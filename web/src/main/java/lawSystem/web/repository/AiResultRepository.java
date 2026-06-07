package lawSystem.web.repository;

import java.util.List;

import lawSystem.jpa.entity.AIAnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ai_analysis_result 에 대한 Spring Data JPA 리포지토리.
 *
 * 기존 BaseRepository(수제) 대신, 웹 계층은 Spring Data JpaRepository 로 통일한다.
 * 인터페이스만 선언하면 CRUD/페이징 구현이 자동 생성된다.
 */
public interface AiResultRepository extends JpaRepository<AIAnalysisResult, String> {

    /** 특정 사건의 AI 분석 결과 이력(최신순). */
    List<AIAnalysisResult> findByLegalCase_CaseIdOrderByGeneratedAtDesc(String caseId);
}
