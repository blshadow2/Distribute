package lawSystem.web.controller;

import java.util.Optional;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lawSystem.jpa.entity.Member;
import lawSystem.web.auth.LoginMember;
import lawSystem.web.auth.SessionConst;
import lawSystem.web.service.AuthService;

/**
 * 로그인/로그아웃/회원가입 + 역할별 대시보드(Thymeleaf).
 */
@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        Optional<Member> member = authService.login(email, password);
        if (member.isEmpty()) {
            model.addAttribute("error", "이메일 또는 비밀번호가 올바르지 않습니다.");
            return "login";
        }
        session.setAttribute(SessionConst.LOGIN_MEMBER, authService.toLoginMember(member.get()));
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String name,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam(required = false) String phoneNumber,
                           @RequestParam(required = false) String address,
                           Model model) {
        try {
            authService.registerClient(name, email, password, phoneNumber, address);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
        return "redirect:/login";
    }

    @GetMapping("/")
    public String dashboard(HttpSession session, Model model) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        model.addAttribute("member", loginMember);
        return "dashboard";
    }

    /** 역할 권한 부족 시 안내 페이지(로그인은 되어 있음, 역할 제한 없음). */
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }

    /** AI 도구 화면(역할에 따라 버튼이 달라짐). 모든 로그인 사용자 접근. */
    @GetMapping("/ai-tools")
    public String aiTools() {
        return "ai-tools";
    }
}
