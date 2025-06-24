package com.tenco.blog._core.errors.exception;

// 403 Forbidden 상황에서 사용할 커스텀 예외
// 로그인은 했지만 해당 리소스에 대한 권한이 없을 때 사용
public class Exception403 extends RuntimeException {

    public Exception403(String msg) {
        super(msg);
    }

    // 사용 예시:
    // throw new Exception403("본인이 작성한 게시글만 수정할 수 있습니다");
    // throw new Exception403("관리자만 접근 가능한 페이지입니다");
}