package com.tenco.blog.user;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import(UserRepository.class)
@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void findByUsernameAndPassword_로그인_성공_테스트() {
        // given: data.sql에 있는 테스트 계정 정보
        String username = "ssar";
        String password = "1234";

        // when: 로그인 시도
        User user = userRepository.findByUsernameAndPassword(username, password);

        // then: 로그인 성공 확인
        Assertions.assertThat(user).isNotNull();
        Assertions.assertThat(user.getUsername()).isEqualTo("ssar");
        Assertions.assertThat(user.getPassword()).isEqualTo("1234");
        Assertions.assertThat(user.getEmail()).isEqualTo("ssar@nate.com");

        System.out.println("로그인 성공 - 사용자: " + user.getUsername());
        System.out.println("이메일: " + user.getEmail());
    }

    @Test
    public void findByUsernameAndPassword_로그인_실패_테스트() {
        // given: 잘못된 계정 정보
        String username = "wronguser";
        String password = "wrongpass";

        // when: 로그인 시도
        User user = userRepository.findByUsernameAndPassword(username, password);

        // then: 로그인 실패 확인 (null 반환)
        Assertions.assertThat(user).isNull();

        System.out.println("로그인 실패 - 일치하는 사용자 없음");
    }

    @Test
    public void findByUsernameAndPassword_비밀번호_틀림_테스트() {
        // given: 사용자명은 맞지만 비밀번호가 틀린 경우
        String username = "ssar";
        String wrongPassword = "wrongpass";

        // when: 로그인 시도
        User user = userRepository.findByUsernameAndPassword(username, wrongPassword);

        // then: 로그인 실패 확인
        Assertions.assertThat(user).isNull();

        System.out.println("로그인 실패 - 비밀번호 불일치");
    }


    @Test
    public void save_회원가입_테스트() {
        // given: 회원가입할 사용자 정보
        User user = User.builder()
                .username("testuser")
                .password("1234")
                .email("test@email.com")
                .build();

        // 저장 전 상태 확인: ID는 null이어야 함
        Assertions.assertThat(user.getId()).isNull();
        System.out.println("저장 전 User: " + user);

        // when: 회원가입 실행 (영속화)
        User savedUser = userRepository.save(user);

        // then: 저장 결과 검증
        // 1. 자동 생성된 ID 확인
        Assertions.assertThat(savedUser.getId()).isNotNull();
        Assertions.assertThat(savedUser.getId()).isGreaterThan(0);

        // 2. 입력한 데이터가 올바르게 저장되었는지 확인
        Assertions.assertThat(savedUser.getUsername()).isEqualTo("testuser");
        Assertions.assertThat(savedUser.getPassword()).isEqualTo("1234");
        Assertions.assertThat(savedUser.getEmail()).isEqualTo("test@email.com");

        // 3. 자동으로 생성된 생성시간 확인
        Assertions.assertThat(savedUser.getCreatedAt()).isNotNull();

        System.out.println("저장 후 User: " + savedUser);

        // 4. 원본 객체와 반환된 객체가 동일한 참조인지 확인
        // 영속성 컨텍스트는 같은 엔티티에 대해 같은 인스턴스를 보장
        Assertions.assertThat(user).isSameAs(savedUser);
    }

    @Test
    public void findByUsername_사용자_조회_테스트() {
        // given: 먼저 사용자를 저장
        User user = User.builder()
                .username("findtest")
                .password("1234")
                .email("find@test.com")
                .build();
        userRepository.save(user);

        // when: 사용자명으로 조회
        User foundUser = userRepository.findByUsername("findtest");

        // then: 조회 결과 검증
        Assertions.assertThat(foundUser).isNotNull();
        Assertions.assertThat(foundUser.getUsername()).isEqualTo("findtest");
        Assertions.assertThat(foundUser.getEmail()).isEqualTo("find@test.com");

        System.out.println("조회된 사용자: " + foundUser.getUsername());
    }

    @Test
    public void findByUsername_존재하지_않는_사용자_테스트() {
        // given: 존재하지 않는 사용자명

        // when: 존재하지 않는 사용자 조회
        User notFoundUser = userRepository.findByUsername("nonexistent");

        // then: null 반환 확인
        Assertions.assertThat(notFoundUser).isNull();
    }
}