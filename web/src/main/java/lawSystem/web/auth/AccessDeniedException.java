package lawSystem.web.auth;

/**
 * 데이터 수준 접근 권한(소유권) 위반 시 던진다.
 * GlobalExceptionHandler 가 페이지 요청은 /access-denied 로, /api/** 요청은 403 으로 매핑한다.
 */
public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}
