package lawSystem.web.config;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lawSystem.web.auth.AccessDeniedException;

/**
 * 소유권/접근 권한 위반(AccessDeniedException)을 분기 처리한다.
 *  - /api/** : HTTP 403 (JSON/텍스트)
 *  - 그 외(페이지) : /access-denied 로 리다이렉트
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public Object handleAccessDenied(AccessDeniedException ex,
                                     HttpServletRequest request,
                                     HttpServletResponse response) throws IOException {
        if (request.getRequestURI().startsWith("/api/")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, ex.getMessage());
            return null;
        }
        return "redirect:/access-denied";
    }
}
