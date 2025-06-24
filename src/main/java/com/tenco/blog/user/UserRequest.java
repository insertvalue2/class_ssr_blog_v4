package com.tenco.blog.user;

import lombok.Data;

// 사용자 관련 요청 데이터를 담는 DTO 클래스
public class UserRequest {

    // 로그인용 DTO
    @Data
    public static class LoginDTO {
        private String username;
        private String password;

        // 로그인 데이터 검증 메서드
        public void validate() {
            if (username == null || username.trim().isEmpty()) {
                throw new IllegalArgumentException("사용자명을 입력해주세요");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("비밀번호를 입력해주세요");
            }
        }
    }

    // 회원가입용 DTO
    @Data
    public static class JoinDTO {
        private String username;
        private String password;
        private String email;

        // DTO에서 User 엔티티로 변환하는 메서드
        // 계층 간 데이터 변환을 명확하게 분리
        public User toEntity() {
            // 빌더 패턴을 사용하여 User 엔티티 생성
            // id와 createdAt은 JPA가 자동으로 설정하므로 제외
            return User.builder()
                    .username(username)
                    .password(password)
                    .email(email)
                    .build();
        }

        // 회원가입 데이터 검증 메서드
        public void validate() {
            if (username == null || username.trim().isEmpty()) {
                throw new IllegalArgumentException("사용자명은 필수입니다");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("비밀번호는 필수입니다");
            }
            if (email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("이메일은 필수입니다");
            }
            // 간단한 이메일 형식 검증
            if (!email.contains("@")) {
                throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다");
            }
        }
    }

    // 회원정보 수정용 DTO
    @Data
    public static class UpdateDTO {
        private String password;
        private String email;
        // username은 제외: 변경 불가능한 고유 식별자

        // 회원정보 수정 데이터 검증 메서드
        public void validate() {
            if (password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("비밀번호는 필수입니다");
            }
            if (password.length() < 4) {
                throw new IllegalArgumentException("비밀번호는 4자 이상이어야 합니다");
            }
//            if (email == null || email.trim().isEmpty()) {
//                throw new IllegalArgumentException("이메일은 필수입니다");
//            }
//            if (!email.contains("@") || !email.contains(".")) {
//                throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다");
//            }
        }
    }
}