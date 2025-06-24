package com.tenco.blog.board;


import com.tenco.blog.user.User;
import lombok.Data;

// 요청 데이터를 담는 DTO 클래스
// 컨트롤러와 비즈니스 로직 사이의 데이터 전송 객체
public class BoardRequest {

    // 게시글 저장용 DTO - V3 수정: User 객체를 매개변수로 받음
    @Data
    public static class SaveDTO {
        private String title;
        private String content;
        // username 필드 제거: 세션에서 User 정보를 가져와서 사용

        // DTO에서 Entity로 변환하는 메서드
        // V3 수정: User 객체를 매개변수로 받아서 연관관계 설정
        public Board toEntity(User user) {
            // 빌더 패턴을 사용하여 Board 엔티티 생성
            // 로그인한 사용자 정보를 연관관계로 설정
            return Board.builder()
                    .title(title)
                    .content(content)
                    .user(user)  // 세션에서 가져온 User 객체 설정
                    .build();
        }

        // 게시글 저장 데이터 검증 메서드
        public void validate() {
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("제목은 필수입니다");
            }
            if (content == null || content.trim().isEmpty()) {
                throw new IllegalArgumentException("내용은 필수입니다");
            }
        }
    }

    // 게시글 수정용 DTO 추가
    @Data
    public static class UpdateDTO {
        private String title;
        private String content;
        private String username;

        // UpdateDTO는 새로운 엔티티를 생성하지 않음
        // 기존 영속 엔티티의 값을 변경하는 용도로만 사용

        // 검증 메서드 (선택사항)
        public void validate() {
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("제목은 필수입니다");
            }
            if (content == null || content.trim().isEmpty()) {
                throw new IllegalArgumentException("내용은 필수입니다");
            }
        }
    }
}
