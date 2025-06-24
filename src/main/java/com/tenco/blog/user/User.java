package com.tenco.blog.user;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@NoArgsConstructor
@Data
@Table(name = "user_tb")
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자명 중복 방지를 위한 유니크 제약조건
    @Column(unique = true)
    private String username;

    private String password;
    private String email;

    // 엔티티가 영속화될 때 자동으로 현재 시간이 설정됨
    @CreationTimestamp
    private Timestamp createdAt;

    // 빌더 패턴: 객체 생성 시 가독성과 안전성 향상
    @Builder
    public User(Long id, String username, String password, String email, Timestamp createdAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.createdAt = createdAt;
    }

    // 회원정보 수정을 위한 비즈니스 메서드
    public void update(UserRequest.UpdateDTO updateDTO) {
        // 비즈니스 규칙 검증
        updateDTO.validate();

        // 영속 상태 엔티티의 필드 값 변경
        // 이 변경사항들이 Dirty Checking 대상이 됨
        this.password = updateDTO.getPassword();
        this.email = updateDTO.getEmail();
        // username은 변경하지 않음 (고유 식별자 역할)

        // 변경 감지(Dirty Checking) 동작 과정:
        // 1. 영속성 컨텍스트가 엔티티 최초 상태를 스냅샷으로 보관
        // 2. 필드 값 변경 시 현재 상태와 스냅샷 비교
        // 3. 트랜잭션 커밋 시점에 변경된 필드만 UPDATE 쿼리 자동 생성
        // 4. UPDATE user_tb SET password=?, email=? WHERE id=?
    }

}