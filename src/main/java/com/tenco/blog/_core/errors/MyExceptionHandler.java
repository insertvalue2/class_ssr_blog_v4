package com.tenco.blog._core.errors;

import com.tenco.blog._core.errors.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

// @ControllerAdvice: 모든 컨트롤러에서 발생하는 예외를 이 클래스에서 처리
// RuntimeException이 발생하면 해당 파일로 예외 처리가 집중됨
@ControllerAdvice
public class MyExceptionHandler {

    // SLF4J 로거 생성 - logback이 구현체로 자동 사용됨
    private static final Logger log = LoggerFactory.getLogger(MyExceptionHandler.class);

    // @ExceptionHandler: 특정 예외 타입이 발생했을 때 실행될 메서드 지정
    // Exception400이 발생하면 이 메서드가 자동 호출됨
    @ExceptionHandler(Exception400.class)
    public String ex400(Exception400 e, HttpServletRequest request) {
        // WARN 레벨: 클라이언트의 잘못된 요청이므로 경고 수준
        log.warn("=== 400 Bad Request 에러 발생 ===");
        log.warn("요청 URL: {}", request.getRequestURL());
        log.warn("에러 메시지: {}", e.getMessage());
        log.warn("예외 클래스: {}", e.getClass().getSimpleName());

        // 예외 메시지를 request에 설정하여 뷰에서 표시
        request.setAttribute("msg", e.getMessage());

        // 400 에러 페이지로 이동
        return "err/400";

        // 동작 과정:
        // 1. 컨트롤러에서 Exception400 발생
        // 2. Spring이 @ControllerAdvice 클래스 스캔
        // 3. @ExceptionHandler(Exception400.class) 메서드 찾기
        // 4. 해당 메서드 실행하여 에러 페이지 반환
    }

    // 401 Unauthorized 처리
    @ExceptionHandler(Exception401.class)
    public String ex401(Exception401 e, HttpServletRequest request) {
        // WARN 레벨: 인증 문제는 보안과 관련되므로 주의 깊게 모니터링
        log.warn("=== 401 Unauthorized 에러 발생 ===");
        log.warn("요청 URL: {}", request.getRequestURL());
        log.warn("인증 오류: {}", e.getMessage());
        log.warn("User-Agent: {}", request.getHeader("User-Agent"));

        request.setAttribute("msg", e.getMessage());
        return "err/401";
    }

    // 403 Forbidden 처리 1
//    @ExceptionHandler(Exception403.class)
//    public String ex403(Exception403 e, HttpServletRequest request) {
//        // WARN 레벨: 권한 문제도 보안 이슈이므로 모니터링 필요
//        log.warn("=== 403 Forbidden 에러 발생 ===");
//        log.warn("요청 URL: {}", request.getRequestURL());
//        log.warn("권한 오류: {}", e.getMessage());
//        log.warn("HTTP 메서드: {}", request.getMethod());
//
//        request.setAttribute("msg", e.getMessage());
//        return "err/403";
//    }

    // 403 Forbidden 처리 2
    @ExceptionHandler(Exception403.class)
    @ResponseBody
    public ResponseEntity<String> ex403(Exception403 e, HttpServletRequest request) {
        log.warn("=== 403 Forbidden 에러 발생 ===");
        log.warn("요청 URL: {}", request.getRequestURL());
        log.warn("권한 오류: {}", e.getMessage());

        // JavaScript 코드를 직접 반환
        String script = "<script>" +
                "alert('" + e.getMessage() + "');" +
                "history.back();" +
                "</script>";

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.TEXT_HTML)
                .body(script);
    }

    // 404 Not Found 처리
    @ExceptionHandler(Exception404.class)
    public String ex404(Exception404 e, HttpServletRequest request) {
        // INFO 레벨: 리소스 없음은 일반적인 상황이므로 정보 수준
        log.info("=== 404 Not Found 에러 발생 ===");
        log.info("요청 URL: {}", request.getRequestURL());
        log.info("리소스 없음: {}", e.getMessage());
        log.info("Referer: {}", request.getHeader("Referer"));

        request.setAttribute("msg", e.getMessage());
        return "err/404";
    }

    // 500 Internal Server Error 처리
    @ExceptionHandler(Exception500.class)
    public String ex500(Exception500 e, HttpServletRequest request) {
        // ERROR 레벨: 서버 오류는 심각한 문제이므로 에러 수준으로 로깅
        log.error("=== 500 Internal Server Error 발생 ===");
        log.error("요청 URL: {}", request.getRequestURL());
        log.error("서버 오류: {}", e.getMessage());
        log.error("스택 트레이스:", e); // 전체 스택 트레이스 포함

        request.setAttribute("msg", e.getMessage());
        return "err/500";
    }

    // 기타 모든 RuntimeException 처리 (최후의 보루)
    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        // ERROR 레벨: 예상치 못한 예외는 심각한 문제
        log.error("=== 예상치 못한 런타임 에러 발생 ===");
        log.error("요청 URL: {}", request.getRequestURL());
        log.error("에러 타입: {}", e.getClass().getSimpleName());
        log.error("에러 메시지: {}", e.getMessage());
        log.error("전체 스택 트레이스:", e); // 디버깅을 위한 전체 스택 트레이스

        request.setAttribute("msg", "시스템 오류가 발생했습니다. 관리자에게 문의해주세요.");
        return "err/500";
    }
}