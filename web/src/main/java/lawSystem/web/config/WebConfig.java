package lawSystem.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lawSystem.web.auth.LoginInterceptor;

/**
 * 로그인 인터셉터 등록. /login, /register, 정적 에러 페이지만 비로그인 허용.
 * (정적 자원/AI 페이지/REST 는 로그인 후에만 접근 — 로그인/회원가입 페이지는 자체 스타일)
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .order(1)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login", "/register", "/error", "/favicon.ico",
                        "/css/**", "/js/**", "/images/**");   // 정적 디자인 자원은 공개
    }
}
