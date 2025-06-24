package com.tenco.blog._core.errors.exception;

// 404 Not Found 상황에서 사용할 커스텀 예외
// 요청한 리소스를 찾을 수 없을 때 사용
public class Exception404 extends RuntimeException {

    public Exception404(String msg) {
        super(msg);
    }

    // 사용 예시:
    // throw new Exception404("요청하신 게시글을 찾을 수 없습니다");
    // throw new Exception404("해당 사용자가 존재하지 않습니다");
}