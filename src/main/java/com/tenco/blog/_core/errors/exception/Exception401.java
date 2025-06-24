package com.tenco.blog._core.errors.exception;

// 401 Unauthorized 상황에서 사용할 커스텀 예외
// 로그인이 필요한 기능에 비로그인 사용자가 접근할 때 사용
public class Exception401 extends RuntimeException {

    public Exception401(String msg) {
        super(msg);
    }

    // 사용 예시:
    // throw new Exception401("로그인이 필요한 서비스입니다");
    // throw new Exception401("세션이 만료되었습니다. 다시 로그인해주세요");
}