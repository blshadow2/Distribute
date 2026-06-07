package lawSystem.web.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 이 핸들러(컨트롤러 클래스 또는 메서드)에 접근 가능한 역할을 제한한다.
 * 값은 LoginMember.viewRole 과 비교한다: CLIENT / PARTNER / ASSOCIATE / STAFF / LAWYER.
 *
 * 예) @RoleAllowed("CLIENT")
 *     @RoleAllowed({"PARTNER", "ASSOCIATE"})
 *
 * 애너테이션이 없으면 "로그인한 모든 사용자" 허용.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RoleAllowed {
    String[] value();
}
