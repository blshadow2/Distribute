package lawSystem.web.auth;

import java.util.Arrays;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 1) 로그인 여부 검사 → 미로그인 시 /login 으로.
 * 2) 핸들러에 {@link RoleAllowed} 가 있으면 세션 역할과 비교 → 불일치 시 차단.
 *    - 페이지 요청: /access-denied 로 리다이렉트
 *    - /api/** 요청: 403 응답 (fetch 클라이언트용)
 */
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // --- 1) 로그인 검사 ---
        HttpSession session = request.getSession(false);
        LoginMember login = (session == null)
                ? null
                : (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);

        if (login == null) {
            response.sendRedirect("/login");
            return false;
        }

        // --- 2) 역할 검사 (@RoleAllowed 가 붙은 컨트롤러 메서드/클래스에만 적용) ---
        if (handler instanceof HandlerMethod handlerMethod) {
            RoleAllowed roleAllowed = handlerMethod.getMethodAnnotation(RoleAllowed.class);
            if (roleAllowed == null) {
                roleAllowed = handlerMethod.getBeanType().getAnnotation(RoleAllowed.class);
            }
            if (roleAllowed != null) {
                boolean permitted = Arrays.asList(roleAllowed.value()).contains(login.getViewRole());
                if (!permitted) {
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "역할 권한이 없습니다.");
                    } else {
                        response.sendRedirect("/access-denied");
                    }
                    return false;
                }
            }
        }
        return true;
    }
}
