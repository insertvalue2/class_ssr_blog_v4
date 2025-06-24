package com.tenco.blog.board;

import com.tenco.blog.utils.MyDateUtil;
import com.tenco.blog.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

// @NoArgsConstructor: JPA에서 엔티티는 기본 생성자가 필요
// JPA가 리플렉션을 통해 객체를 생성할 때 사용
@NoArgsConstructor
@Data
@Table(name = "board_tb")
@Entity
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // V2와 일관성 유지

    private String title;
    private String content;

    // V2에서 제거: private String username;
    // V3에서 추가: User 엔티티와의 연관관계
    // 다대일 연관관계: 여러 게시글이 하나의 사용자에게 속함
    // FetchType.LAZY: 지연로딩으로 성능 최적화 (User 정보가 필요할 때만 조회)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // 외래키 컬럼명 명시
    private User user;

    // @CreationTimestamp: Hibernate가 제공하는 어노테이션
    // 엔티티가 처음 저장될 때 현재 시간을 자동으로 설정
    // V1에서는 SQL에서 now()를 직접 사용했지만, V2에서는 JPA가 자동 처리
    // pc -> db (날짜주입)
    @CreationTimestamp
    private Timestamp createdAt;

    // 빌더 패턴을 위한 생성자
    // @Builder 어노테이션으로 Board.builder() 형태의 빌더 생성 가능
    // 객체 생성 시 가독성과 안전성 향상
    @Builder
    public Board(Long id, String title, String content, User user, Timestamp createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.user = user;
        this.createdAt = createdAt;
    }

    // V2에서 유지: 시간 포맷팅 메서드
    public String getTime(){
        return MyDateUtil.timestampFormat(createdAt);
    }

    // 영속 엔티티 수정을 위한 비즈니스 메서드
    // V3 수정: username 제거, User는 수정하지 않음 (작성자 변경 불가)
    public void update(BoardRequest.UpdateDTO updateDTO) {
        // 비즈니스 규칙 검증
        updateDTO.validate();

        // 영속 상태 엔티티의 필드 값 변경
        // 이 변경사항들이 Dirty Checking 대상이 됨
        this.title = updateDTO.getTitle();
        this.content = updateDTO.getContent();
        // username 제거: 작성자는 변경할 수 없음

        // 변경 감지(Dirty Checking) 동작 과정:
        // 1. 영속성 컨텍스트가 엔티티 최초 상태를 스냅샷으로 보관
        // 2. 필드 값 변경 시 현재 상태와 스냅샷 비교
        // 3. 트랜잭션 커밋 시점에 변경된 필드만 UPDATE 쿼리 자동 생성
        // 4. UPDATE board_tb SET title=?, content=? WHERE id=?
    }

    // 개별 필드 수정 메서드 (필요시 사용)
    public void updateTitle(String newTitle) {
        if (newTitle == null || newTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("제목은 필수입니다");
        }
        this.title = newTitle;
    }

    public void updateContent(String newContent) {
        if (newContent == null || newContent.trim().isEmpty()) {
            throw new IllegalArgumentException("내용은 필수입니다");
        }
        this.content = newContent;
    }

    // V3 추가: 게시글 소유자 확인을 위한 비즈니스 메서드
    // 게시글 수정/삭제 권한 체크에 사용
    public boolean isOwner(Long userId) {
        return this.user.getId().equals(userId);
    }

    // V3 추가: 작성자명 빠른 접근을 위한 편의 메서드
    // 기존 username 필드를 대체하여 뷰에서 작성자명 표시할 때 사용
    public String getWriterName() {
        return this.user.getUsername();
    }
}