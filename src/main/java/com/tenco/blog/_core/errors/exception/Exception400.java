package com.tenco.blog._core.errors.exception;

// 400 Bad Request 상황에서 사용할 커스텀 예외
// RuntimeException을 상속하여 언체크 예외로 만듦
public class Exception400 extends RuntimeException {

    // 예외 메시지를 받는 생성자
    public Exception400(String msg) {
        super(msg);  // 부모 클래스의 메시지 설정
    }

    // 사용 예시:
    // throw new Exception400("필수 입력 항목이 누락되었습니다");
    // throw new Exception400("올바르지 않은 데이터 형식입니다");
}