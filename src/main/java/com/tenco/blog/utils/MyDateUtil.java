package com.tenco.blog.utils;


import org.apache.commons.lang3.time.DateFormatUtils;

import java.sql.Timestamp;
import java.util.Date;

// 날짜/시간 관련 유틸리티 클래스
// static 메서드로 구성하여 객체 생성 없이 사용 가능
// 유틸리티 클래스 패턴: 공통 기능을 static 메서드로 제공
public class MyDateUtil {

    // Timestamp를 원하는 포맷의 문자열로 변환
    public static String timestampFormat(Timestamp time){
        // Timestamp를 Date 객체로 변환
        // getTime(): 밀리초 단위의 시간값 반환
        Date currentDate = new Date(time.getTime());

        // DateFormatUtils: Apache Commons Lang3 라이브러리의 유틸리티
        // 다양한 날짜 포맷을 간편하게 적용 가능
        return DateFormatUtils.format(currentDate, "yyyy-MM-dd HH:mm");
    }
}