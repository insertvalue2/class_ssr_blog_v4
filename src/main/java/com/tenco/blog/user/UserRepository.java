package com.tenco.blog.user;


import com.tenco.blog._core.errors.exception.Exception404;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Repository
public class UserRepository {

    private static final Logger log = LoggerFactory.getLogger(UserRepository.class);

    private final EntityManager em;

    // JPA 영속화를 통한 회원가입
    @Transactional
    public User save(User user) {
        log.info("회원가입 처리 시작 - 사용자명: {}", user.getUsername());

        // 비영속 상태의 User 엔티티를 영속성 컨텍스트에 저장
        em.persist(user);

        log.info("회원가입 영속화 완료 - ID: {}, 사용자명: {}", user.getId(), user.getUsername());
        return user;
    }

    // 회원정보 조회: 수정 폼용
    public User findById(Long id) {
        User user = em.find(User.class, id);

        if (user == null) {
            log.warn("존재하지 않는 사용자 조회 시도 - ID: {}", id);
            throw new Exception404("사용자를 찾을 수 없습니다. ID: " + id);
        }

        log.debug("사용자 조회 완료 - ID: {}, 사용자명: {}", user.getId(), user.getUsername());
        return user;
    }

    // Dirty Checking을 활용한 회원정보 수정
    @Transactional
    public User updateById(Long id, UserRequest.UpdateDTO reqDTO) {
        log.info("회원정보 수정 시작 - 사용자 ID: {}", id);

        // 1. 수정할 사용자를 영속 상태로 조회 (Exception404 자동 처리)
        User user = findById(id);

        log.info("수정 전 정보 - 사용자명: {}, 이메일: {}", user.getUsername(), user.getEmail());

        // 2. 영속 상태 엔티티의 값 변경 (Dirty Checking 시작)
        user.update(reqDTO);

        log.info("수정 후 정보 - 사용자명: {}, 이메일: {}", user.getUsername(), user.getEmail());
        log.info("회원정보 수정 완료 - 사용자 ID: {}", id);

        // 수정된 영속 엔티티 반환 (세션 동기화용)
        return user;
    }

    // 로그인용 사용자 조회
    public User findByUsernameAndPassword(String username, String password) {
        try {
            String jpql = "SELECT u FROM User u WHERE u.username = :username AND u.password = :password";
            User user = em.createQuery(jpql, User.class)
                    .setParameter("username", username)
                    .setParameter("password", password)
                    .getSingleResult();

            log.info("로그인 인증 성공 - 사용자명: {}", username);
            return user;

        } catch (Exception e) {
            log.warn("로그인 인증 실패 - 사용자명: {}", username);
            return null;
        }
    }

    // 사용자명 중복 체크용 조회 메서드
    public User findByUsername(String username) {
        try {
            String jpql = "SELECT u FROM User u WHERE u.username = :username";
            User user = em.createQuery(jpql, User.class)
                    .setParameter("username", username)
                    .getSingleResult();

            log.debug("사용자명 조회 성공 - 사용자명: {}", username);
            return user;

        } catch (Exception e) {
            log.debug("사용자명 조회 결과 없음 - 사용자명: {}", username);
            return null;
        }
    }
}