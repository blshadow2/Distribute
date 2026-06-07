package lawSystem.web.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lawSystem.jpa.entity.AssociateLawyer;
import lawSystem.jpa.entity.Client;
import lawSystem.jpa.entity.Lawyer;
import lawSystem.jpa.entity.Member;
import lawSystem.jpa.entity.PartnerLawyer;
import lawSystem.jpa.entity.Staff;
import lawSystem.web.auth.LoginMember;
import lawSystem.web.repository.MemberRepository;

/**
 * 인증 서비스: 회원 가입(의뢰인) / 로그인(BCrypt) / 세션 사용자 변환.
 *
 * 변호사·사무직원 계정은 보통 운영자가 시드/생성하므로, 가입은 의뢰인(Client)만 제공한다.
 */
@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /** 의뢰인 가입. 이메일 중복 시 예외. */
    @Transactional
    public Client registerClient(String name, String email, String rawPassword,
                                 String phoneNumber, String address) {
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        String memberId = "member-" + UUID.randomUUID().toString().substring(0, 8);
        Client client = new Client(memberId, name, email,
                passwordEncoder.encode(rawPassword), phoneNumber, address);
        memberRepository.save(client);
        return client;
    }

    /** 로그인: 이메일로 조회 후 BCrypt 비교. 성공 시 Member 반환. */
    @Transactional(readOnly = true)
    public Optional<Member> login(String email, String rawPassword) {
        return memberRepository.findByEmail(email)
                .filter(m -> passwordEncoder.matches(rawPassword, m.getPassword()));
    }

    /** 세션 저장용 LoginMember(화면 분기 역할 포함)로 변환. */
    public LoginMember toLoginMember(Member m) {
        return new LoginMember(m.getMemberId(), m.getName(), computeViewRole(m));
    }

    private String computeViewRole(Member m) {
        if (m instanceof Client) return "CLIENT";
        if (m instanceof Staff) return "STAFF";
        if (m instanceof PartnerLawyer) return "PARTNER";
        if (m instanceof AssociateLawyer) return "ASSOCIATE";
        if (m instanceof Lawyer) return "LAWYER";
        return m.getRole();
    }
}
