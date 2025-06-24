package com.tenco.blog.user;


import com.tenco.blog._core.errors.exception.Exception400;
import com.tenco.blog._core.errors.exception.Exception401;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@RequiredArgsConstructor
@Controller
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserRepository userRepository;

    // 회원정보 수정 폼 페이지: 기존 데이터로 폼 미리 채우기
    @GetMapping("/user/update-form")
    public String updateForm(HttpServletRequest request, HttpSession session) {

        log.info("회원정보 수정 폼 요청");

        // 1. 로그인 체크: 로그인하지 않은 사용자는 접근 불가
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            log.warn("비로그인 사용자의 회원정보 수정 폼 접근 시도");
            // @ControllerAdvice에서 401 에러 페이지 처리됨
            throw new Exception401("회원정보를 수정하려면 먼저 로그인해주세요.");
        }

        log.info("회원정보 수정 폼 요청자: {}", sessionUser.getUsername());

        // 2. 최신 사용자 정보 조회 (세션 정보는 옛날 것일 수 있음)
        User user = userRepository.findById(sessionUser.getId());

        log.info("최신 사용자 정보 조회 완료 - 사용자: {}", user.getUsername());

        // 3. 수정 폼에 기존 데이터 전달 (미리 채우기용)
        // 주의: 비밀번호는 보안상 전달하지 않음
        request.setAttribute("user", user);

        return "user/update-form";
    }

    // 회원정보 수정 처리: Dirty Checking과 세션 동기화
    @PostMapping("/user/update")
    public String update(UserRequest.UpdateDTO reqDTO, HttpSession session) {

        log.info("회원정보 수정 요청 - 새 이메일: {}", reqDTO.getEmail());

        // 1. 로그인 체크
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            log.warn("비로그인 사용자의 회원정보 수정 시도");
            throw new Exception401("회원정보를 수정하려면 먼저 로그인해주세요.");
        }

        // 2. 입력 데이터 검증 (Exception400 자동 처리됨)
        reqDTO.validate();

        log.info("입력 데이터 검증 완료 - 요청자: {}", sessionUser.getUsername());

        // 3. Dirty Checking을 통한 회원정보 수정
        User updatedUser = userRepository.updateById(sessionUser.getId(), reqDTO);

        log.info("회원정보 수정 완료 - 사용자: {}, 새 이메일: {}",
                updatedUser.getUsername(), updatedUser.getEmail());

        // 4. 세션 동기화: 수정된 정보를 세션에 반영
        session.setAttribute("sessionUser", updatedUser);

        log.info("세션 동기화 완료 - 사용자: {}", updatedUser.getUsername());

        // 5. 수정 완료 후 메인 페이지로 리다이렉트
        return "redirect:/";
    }

    // 로그인 폼 페이지
    @GetMapping("/login-form")
    public String loginForm() {
        log.info("로그인 폼 페이지 요청");
        return "user/login-form";
    }

    // 로그인 처리
    @PostMapping("/login")
    public String login(UserRequest.LoginDTO loginDTO, HttpSession session) {

        log.info("로그인 시도 - 사용자명: {}", loginDTO.getUsername());

        // 1. 입력 데이터 검증 (Exception400 자동 처리됨)
        loginDTO.validate();

        // 2. 사용자명과 비밀번호로 사용자 조회
        User sessionUser = userRepository.findByUsernameAndPassword(
                loginDTO.getUsername(),
                loginDTO.getPassword()
        );

        // 3. 로그인 성공/실패 처리
        if (sessionUser == null) {
            // 로그인 실패: 일치하는 사용자 없음
            log.warn("로그인 실패 - 사용자명: {}, 원인: 인증 정보 불일치", loginDTO.getUsername());
            throw new Exception401("사용자명 또는 비밀번호가 올바르지 않습니다.");
        }

        // 4. 로그인 성공: 세션에 사용자 정보 저장
        session.setAttribute("sessionUser", sessionUser);

        log.info("로그인 성공 - 사용자: {}, 세션 ID: {}",
                sessionUser.getUsername(), session.getId());

        // 5. 메인 페이지로 리다이렉트
        // (HTTP 헤더는 ASCII만 허용)
        // 공백은 URL에서 유효하지 않음
        return "redirect:/";
    }

    // 로그아웃 처리
    @GetMapping("/logout")
    public String logout(HttpSession session) {

        log.info("로그아웃 요청");

        // 현재 로그인한 사용자 정보 출력 (로그아웃 전)
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser != null) {
            log.info("로그아웃 처리 - 사용자: {}", sessionUser.getUsername());
        } else {
            log.warn("이미 로그아웃된 상태에서 로그아웃 요청");
        }

        // 세션 무효화: 모든 세션 데이터 제거
        session.invalidate();

        log.info("로그아웃 완료 - 세션 무효화됨");

        // 메인 페이지로 리다이렉트
        return "redirect:/";
    }

    // 회원가입 폼 페이지
    @GetMapping("/join-form")
    public String joinForm() {
        log.info("회원가입 폼 페이지 요청");
        return "user/join-form";
    }

    // 회원가입 처리
    @PostMapping("/join")
    public String join(UserRequest.JoinDTO joinDTO) {

        log.info("회원가입 요청 - 사용자명: {}, 이메일: {}",
                joinDTO.getUsername(), joinDTO.getEmail());

        // 1. 입력 데이터 검증 (Exception400 자동 처리됨)
        joinDTO.validate();

        // 2. 사용자명 중복 체크
        User existingUser = userRepository.findByUsername(joinDTO.getUsername());
        if (existingUser != null) {
            log.warn("회원가입 실패 - 중복된 사용자명: {}", joinDTO.getUsername());
            throw new Exception400("이미 존재하는 사용자명입니다: " + joinDTO.getUsername());
        }

        // 3. DTO를 Entity로 변환
        User user = joinDTO.toEntity();

        log.info("User 엔티티 생성 완료 - 사용자명: {}", user.getUsername());

        // 4. User 엔티티 영속화 (회원가입 완료)
        User savedUser = userRepository.save(user);

        log.info("회원가입 완료 - ID: {}, 사용자명: {}, 생성시간: {}",
                savedUser.getId(), savedUser.getUsername(), savedUser.getCreatedAt());

        // 5. 회원가입 성공 시 로그인 페이지로 리다이렉트
        return "redirect:/login-form";
    }
}