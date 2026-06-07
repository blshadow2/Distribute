package lawSystem.web.repository;

import java.util.Optional;

import lawSystem.jpa.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 회원 리포지토리. Member 는 추상(JOINED)이지만 Spring Data 가
 * 구체 타입(Client/Lawyer/Staff)을 반환한다.
 */
public interface MemberRepository extends JpaRepository<Member, String> {

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);
}
