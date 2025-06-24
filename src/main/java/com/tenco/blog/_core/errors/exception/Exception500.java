package com.tenco.blog._core.errors.exception;

// 500 Internal Server Error 상황에서 사용할 커스텀 예외
// 서버 내부 오류나 예상치 못한 상황에서 사용
public class Exception500 extends RuntimeException {

    public Exception500(String msg) {
        super(msg);
    }

    // 사용 예시:
    // throw new Exception500("데이터베이스 연결에 실패했습니다");
    // throw new Exception500("파일 처리 중 오류가 발생했습니다");
}

