package com.tenco.blog.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Repository
public class UserRepository {
    private final EntityManager em;

    // Dirty Checking을 활용한 회원정보 수정
    @Transactional
    public User updateById(Long id, UserRequest.UpdateDTO reqDTO) {
        // 1. 수정할 사용자를 영속 상태로 조회
        User user = findById(id);  // 영속성 컨텍스트에서 관리되는 엔티티

        System.out.println("=== 회원정보 수정 시작 ===");
        System.out.println("수정 대상: " + user.getUsername());
        System.out.println("수정 전 이메일: " + user.getEmail());

        // 2. 영속 상태 엔티티의 값 변경 (Dirty Checking 시작)
        // user.setPassword(reqDTO.getPassword());
        user.update(reqDTO);

        System.out.println("=== 엔티티 값 변경 완료 ===");
        System.out.println("수정 후 비밀번호 확인 : " + user.getPassword());

        // 3. persist() 호출 불필요!
        // 트랜잭션 커밋 시점에 영속성 컨텍스트가 자동으로 변경 감지
        // 변경된 필드만 UPDATE 쿼리 자동 생성 및 실행

        // 4. 수정된 영속 엔티티 반환 (세션 동기화용)
        return user;

        // 세션 동기화가 중요한 이유:
        // 회원정보가 수정되었는데 세션 정보가 옛날 것이면
        // 사용자는 여전히 옛날 정보를 보게 됨
    }

    // 회원정보 조회: 수정 폼용
    public User findById(Long id) {
        User user = em.find(User.class, id);

        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다. ID: " + id);
        }

        System.out.println("=== 회원정보 조회 ===");
        System.out.println("조회된 사용자: " + user.getUsername());
        System.out.println("이메일: " + user.getEmail());

        return user;
    }



    // 로그인용 사용자 조회: 사용자명과 비밀번호로 검증
    public User findByUsernameAndPassword(String username, String password) {
        try {
            // JPQL로 사용자명과 비밀번호가 일치하는 사용자 조회
            String jpql = "SELECT u FROM User u WHERE u.username = :username AND u.password = :password";

            Query query = em.createQuery(jpql, User.class);
            query.setParameter("username", username);
            query.setParameter("password", password);

            return (User) query.getSingleResult();

        } catch (Exception e) {
            // 일치하는 사용자가 없거나 에러 발생 시 null 반환
            // 로그인 실패를 의미함
            return null;
        }
    }


    // 회원가입: User 엔티티 영속화
    @Transactional
    public User save(User user) {
        // 비영속 상태의 User 엔티티를 영속성 컨텍스트에 저장
        // 영속성 컨텍스트가 user 객체를 관리하기 시작
        em.persist(user);

        // persist() 후 user 객체는 영속 상태가 됨
        // 트랜잭션 커밋 시점에 실제 INSERT 쿼리 실행
        // 자동 생성된 ID와 생성시간이 user 객체에 설정됨
        return user;
    }

    // 사용자명 중복 체크용 조회 메서드
    public User findByUsername(String username) {
        try {
            String jpql = "SELECT u FROM User u WHERE u.username = :username";
            return em.createQuery(jpql, User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (Exception e) {
            // 사용자를 찾을 수 없는 경우 null 반환
            return null;
        }
    }
}