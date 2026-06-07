package lawSystem.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 법률 사무 플랫폼 웹 애플리케이션 진입점이다.
 *
 * - 엔티티는 기존 lawSystem.jpa.entity 패키지를 그대로 스캔한다.
 * - 리포지토리는 lawSystem.web.repository 에 둔다.
 * - 기존 도메인/AI(lawSystem.ai)·DAO(lawSystem.db.dao)는 서비스 계층에서 직접 사용한다.
 */
@SpringBootApplication
@EntityScan("lawSystem.jpa.entity")
@EnableJpaRepositories("lawSystem.web.repository")
public class LawSystemWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(LawSystemWebApplication.class, args);
    }
}
