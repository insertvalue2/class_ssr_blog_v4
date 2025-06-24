package com.tenco.blog._core.config;

import com.tenco.blog._core.interceptor.LoginInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // 하나의 클래스를 IOC 하고 싶다면 사용
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    // final 키워드로 불변 객체임을 보장
    private final LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // LoginInterceptor를 시스템에 등록
        registry.addInterceptor(loginInterceptor)
                // 인터셉터가 동작할 URL 패턴을 지정
                .addPathPatterns("/board/**", "/user/**")
                // 인터셉터에서 제외할 URL 패턴을 지정
                .excludePathPatterns("/board/{id:\\d+}");
                // \\d+ 는 정규표현식으로 "1개 이상의 숫자"를 의미
                // 예: /board/1, /board/123 등은 로그인 없이도 접근 가능

    }
}