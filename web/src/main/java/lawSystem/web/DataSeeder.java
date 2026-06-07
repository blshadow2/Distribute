package lawSystem.web;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lawSystem.jpa.entity.AssociateLawyer;
import lawSystem.jpa.entity.Client;
import lawSystem.jpa.entity.PartnerLawyer;
import lawSystem.jpa.entity.Staff;
import lawSystem.web.repository.MemberRepository;

/**
 * 앱 시작 시 역할별 테스트 계정을 1회 생성한다(이미 있으면 건너뜀).
 * 의뢰인 폼 가입 외에 변호사/사무직원 대시보드를 확인하기 위한 시드.
 *
 * 모든 계정 비밀번호: pw1234  (BCrypt 해시 저장)
 *
 * 운영 전환 시: 이 클래스를 삭제하거나 @Profile("dev") 로 제한하면 된다.
 * (DB 가 JPA 스키마로 통합되어 있어야 정상 동작 — 아니면 로그만 남기고 앱은 계속 뜸)
 */
@Component
@Order(1)
public class DataSeeder implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    public DataSeeder(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public void run(String... args) {
        seed("client@law.test", () -> new Client(
                "seed-client", "의뢰인 김의뢰", "client@law.test", pw(), "010-1000-0001", "서울시 강남구"));

        seed("partner@law.test", () -> new PartnerLawyer(
                "seed-partner", null, "대표 박파트너", "partner@law.test", pw(), "010-2000-0001",
                "LAW-P-0001", "서울 본사", List.of("개인정보", "민사"), "대표 변호사입니다."));

        seed("associate@law.test", () -> new AssociateLawyer(
                "seed-associate", "소속 이소속", "associate@law.test", pw(), "010-3000-0001",
                "LAW-A-0001", "서울 본사", List.of("개인정보", "민사"), "개인정보·민사 사건 담당입니다."));

        seed("associate2@law.test", () -> new AssociateLawyer(
                "seed-assoc2", "변호사 정형사", "associate2@law.test", pw(), "010-3000-0002",
                "LAW-A-0002", "부산 사무소", List.of("형사", "교통"), "형사·교통 사건 전문입니다."));

        seed("associate3@law.test", () -> new AssociateLawyer(
                "seed-assoc3", "변호사 한노동", "associate3@law.test", pw(), "010-3000-0003",
                "LAW-A-0003", "대구 사무소", List.of("노동", "행정"), "노동·행정 사건 전문입니다."));

        seed("staff@law.test", () -> new Staff(
                "seed-staff", "직원 최사무", "staff@law.test", pw(), "010-4000-0001",
                "법무지원팀", "사무원"));
    }

    private String pw() {
        return encoder.encode("pw1234");
    }

    private void seed(String email, java.util.function.Supplier<? extends Object> factory) {
        try {
            if (memberRepository.existsByEmail(email)) {
                return;
            }
            memberRepository.save((lawSystem.jpa.entity.Member) factory.get());
            System.out.println("[Seed] 계정 생성: " + email + " (비밀번호 pw1234)");
        } catch (Exception e) {
            // DB 스키마 미통합 등으로 실패해도 앱 기동은 계속한다.
            System.err.println("[Seed] " + email + " 생성 건너뜀: " + e.getMessage());
        }
    }
}
