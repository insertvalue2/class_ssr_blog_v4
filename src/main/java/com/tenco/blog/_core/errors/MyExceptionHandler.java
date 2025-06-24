package com.tenco.blog._core.errors;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import com.tenco.blog._core.errors.exception.*;

// @ControllerAdvice: 모든 컨트롤러에서 발생하는 예외를 이 클래스에서 처리
// RuntimeException이 발생하면 해당 파일로 예외 처리가 집중됨
@ControllerAdvice
public class MyExceptionHandler {

    // @ExceptionHandler: 특정 예외 타입이 발생했을 때 실행될 메서드 지정
    // Exception400이 발생하면 이 메서드가 자동 호출됨
    @ExceptionHandler(Exception400.class)
    public String ex400(Exception400 e, HttpServletRequest request) {
        System.out.println("=== 400 에러 처리 ===");
        System.out.println("에러 메시지: " + e.getMessage());

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
        System.out.println("=== 401 에러 처리 ===");
        System.out.println("인증 오류: " + e.getMessage());

        request.setAttribute("msg", e.getMessage());
        return "err/401";
    }

    // 403 Forbidden 처리
    @ExceptionHandler(Exception403.class)
    public String ex403(Exception403 e, HttpServletRequest request) {
        System.out.println("=== 403 에러 처리 ===");
        System.out.println("권한 오류: " + e.getMessage());

        request.setAttribute("msg", e.getMessage());
        return "err/403";
    }

    // 404 Not Found 처리
    @ExceptionHandler(Exception404.class)
    public String ex404(Exception404 e, HttpServletRequest request) {
        System.out.println("=== 404 에러 처리 ===");
        System.out.println("리소스 없음: " + e.getMessage());

        request.setAttribute("msg", e.getMessage());
        return "err/404";
    }

    // 500 Internal Server Error 처리
    @ExceptionHandler(Exception500.class)
    public String ex500(Exception500 e, HttpServletRequest request) {
        System.out.println("=== 500 에러 처리 ===");
        System.out.println("서버 오류: " + e.getMessage());

        request.setAttribute("msg", e.getMessage());
        return "err/500";
    }

    // 기타 모든 RuntimeException 처리 (최후의 보루)
    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        System.out.println("=== 예상치 못한 런타임 에러 ===");
        System.out.println("에러 타입: " + e.getClass().getSimpleName());
        System.out.println("에러 메시지: " + e.getMessage());

        request.setAttribute("msg", "시스템 오류가 발생했습니다. 관리자에게 문의해주세요.");
        return "err/500";
    }
}