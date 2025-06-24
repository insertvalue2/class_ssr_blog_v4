package com.tenco.blog.utils;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.junit.jupiter.api.Test;
import java.sql.Timestamp;
import java.util.Date;

public class DateTest {

    // TDD(Test Driven Development) 접근법
    // 1. 테스트 코드 먼저 작성
    // 2. 테스트를 통과하는 최소한의 코드 작성
    // 3. 리팩토링으로 코드 개선
    @Test
    public void timestampFormat_test(){
        // 1. 내가 만든 MyDateUtil 클래스 테스트
        // given: 현재 시간으로 Timestamp 객체 생성
        // System.currentTimeMillis(): 1970년 1월 1일부터의 밀리초
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());

        // when: 유틸리티 메서드로 포맷 변환
        // 2025-11-11 15:27 형태 확인
        String createdAt = MyDateUtil.timestampFormat(currentTimestamp);

        // then: 결과 확인
        System.out.println("timestampFormat_test : "+ createdAt);

        // 추가 검증: 포맷이 올바른지 확인
        // 정규표현식을 사용한 포맷 검증
        // \\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2} 패턴 확인
        // Assertions.assertThat(createdAt).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}");
    }

    @Test
    public void format_test(){
        // 2. Apache Commons Lang3의 DateFormatUtils 직접 사용 테스트
        // 외부 라이브러리 동작 확인 및 학습 목적
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        Date currentDate = new Date(currentTimestamp.getTime());
        // import org.apache.commons.lang3.time.DateFormatUtils; 의존성으로 추가한 패키지 확인
        String formattedDate = DateFormatUtils.format(currentDate, "yyyy-MM-dd HH:mm");

        System.out.println("format_test : " + formattedDate);
    }

}