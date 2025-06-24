package com.tenco.blog.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@RequiredArgsConstructor
@Controller
public class UserController {

    private final UserRepository userRepository;

    // 회원정보 수정 폼 페이지: 기존 데이터로 폼 미리 채우기
    @GetMapping("/user/update-form")
    public String updateForm(HttpServletRequest request, HttpSession session) {

        System.out.println("=== 회원정보 수정 폼 요청 ===");

        // 1. 로그인 체크: 로그인하지 않은 사용자는 접근 불가
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            System.out.println("로그인하지 않은 사용자의 회원정보 수정 시도");
            return "redirect:/login-form";
        }

        System.out.println("수정 요청자: " + sessionUser.getUsername());

        // 2. 최신 사용자 정보 조회 (세션 정보는 옛날 것일 수 있음)
        User user = userRepository.findById(sessionUser.getId());

        System.out.println("최신 사용자 정보 조회 완료");

        // 3. 수정 폼에 기존 데이터 전달 (미리 채우기용)
        // 주의: 비밀번호는 보안상 전달하지 않음
        request.setAttribute("user", user);

        return "user/update-form";
    }

    // 회원정보 수정 처리: Dirty Checking과 세션 동기화
    @PostMapping("/user/update")
    public String update(UserRequest.UpdateDTO reqDTO, HttpSession session, HttpServletRequest request) {

        System.out.println("=== 회원정보 수정 요청 ===");

        // 1. 로그인 체크
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            System.out.println("로그인하지 않은 사용자의 수정 시도");
            return "redirect:/login-form";
        }

        System.out.println("수정 요청자: " + sessionUser.getUsername());
        System.out.println("새 이메일: " + reqDTO.getEmail());

        try {
            // 2. 입력 데이터 검증
            reqDTO.validate();
            System.out.println("입력 데이터 검증 완료");

            // 3. Dirty Checking을 통한 회원정보 수정
            User updatedUser = userRepository.updateById(sessionUser.getId(), reqDTO);

            System.out.println("=== 회원정보 수정 완료 ===");
            System.out.println("수정된 사용자 ID: " + updatedUser.getId());
            System.out.println("최종 이메일: " + updatedUser.getEmail());

            // 4. 세션 동기화: 수정된 정보를 세션에 반영
            session.setAttribute("sessionUser", updatedUser);

            System.out.println("=== 세션 동기화 완료 ===");
            System.out.println("세션이 최신 정보로 업데이트됨");

            // 5. 수정 완료 후 메인 페이지로 리다이렉트
            return "redirect:/?success=update";

        } catch (IllegalArgumentException e) {
            // 검증 실패 시 에러 메시지와 함께 수정 폼으로 돌아가기
            System.out.println("회원정보 수정 실패: " + e.getMessage());
            request.setAttribute("errorMessage", e.getMessage());

            // 수정 폼에 기존 데이터 다시 전달
            User user = userRepository.findById(sessionUser.getId());
            request.setAttribute("user", user);

            return "user/update-form";
        }
    }


    // 로그인 폼 페이지 (추후 구현 예정)
    @GetMapping("/login-form")
    public String loginForm() {
        return "user/login-form";
    }

    // 로그인 처리
    @PostMapping("/login")
    public String login(UserRequest.LoginDTO loginDTO, HttpSession session, HttpServletRequest request) {

        System.out.println("=== 로그인 시도 ===");
        System.out.println("사용자명: " + loginDTO.getUsername());

        try {
            // 1. 입력 데이터 검증
            loginDTO.validate();

            // 2. 사용자명과 비밀번호로 사용자 조회
            User sessionUser = userRepository.findByUsernameAndPassword(
                    loginDTO.getUsername(),
                    loginDTO.getPassword()
            );

            // 3. 로그인 성공/실패 처리
            if (sessionUser == null) {
                // 로그인 실패: 일치하는 사용자 없음
                throw new IllegalArgumentException("사용자명 또는 비밀번호가 올바르지 않습니다");
            }

            // 4. 로그인 성공: 세션에 사용자 정보 저장
            session.setAttribute("sessionUser", sessionUser);

            System.out.println("=== 로그인 성공 ===");
            System.out.println("로그인한 사용자: " + sessionUser.getUsername());
            System.out.println("세션 ID: " + session.getId());

            // 5. 메인 페이지로 리다이렉트
            return "redirect:/";

        } catch (IllegalArgumentException e) {
            // 로그인 실패 시 에러 메시지와 함께 로그인 폼으로 돌아가기
            System.out.println("로그인 실패: " + e.getMessage());
            request.setAttribute("errorMessage", e.getMessage());
            return "user/login-form";
        }
    }

    // 로그아웃 처리
    @GetMapping("/logout")
    public String logout(HttpSession session) {

        System.out.println("=== 로그아웃 요청 ===");

        // 현재 로그인한 사용자 정보 출력 (로그아웃 전)
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser != null) {
            System.out.println("로그아웃할 사용자: " + sessionUser.getUsername());
        }

        // 세션 무효화: 모든 세션 데이터 제거
        session.invalidate();

        System.out.println("=== 로그아웃 완료 ===");
        System.out.println("세션이 무효화되었습니다");

        // 메인 페이지로 리다이렉트
        return "redirect:/";
    }


    // 회원가입 폼 페이지
    @GetMapping("/join-form")
    public String joinForm() {
        return "user/join-form";
    }

    // 회원가입 처리
    @PostMapping("/join")
    public String join(UserRequest.JoinDTO joinDTO, HttpServletRequest request) {

        System.out.println("=== 회원가입 요청 ===");
        System.out.println("사용자명: " + joinDTO.getUsername());
        System.out.println("이메일: " + joinDTO.getEmail());

        try {
            // 1. 입력 데이터 검증
            joinDTO.validate();

            // 2. 사용자명 중복 체크
            User existingUser = userRepository.findByUsername(joinDTO.getUsername());
            if (existingUser != null) {
                throw new IllegalArgumentException("이미 존재하는 사용자명입니다: " + joinDTO.getUsername());
            }

            // 3. DTO를 Entity로 변환
            User user = joinDTO.toEntity();

            System.out.println("=== User 엔티티 생성 완료 ===");
            System.out.println("변환된 User: " + user.getUsername());

            // 4. User 엔티티 영속화 (회원가입 완료)
            User savedUser = userRepository.save(user);

            System.out.println("=== 회원가입 완료 ===");
            System.out.println("생성된 ID: " + savedUser.getId());
            System.out.println("생성 시간: " + savedUser.getCreatedAt());

            // 5. 회원가입 성공 시 로그인 페이지로 리다이렉트
            return "redirect:/login-form";

        } catch (IllegalArgumentException e) {
            // 검증 실패 시 에러 메시지와 함께 회원가입 폼으로 돌아가기
            System.out.println("회원가입 실패: " + e.getMessage());
            request.setAttribute("errorMessage", e.getMessage());
            return "user/join-form";
        }
    }


}