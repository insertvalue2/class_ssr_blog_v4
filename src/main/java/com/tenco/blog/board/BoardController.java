package com.tenco.blog.board;


import com.tenco.blog.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BoardController {

    private final BoardRepository boardRepository;

    // 게시글 수정 폼 페이지: 기존 데이터로 폼 미리 채우기
    @GetMapping("/board/{id}/update-form")
    public String updateForm(@PathVariable("id") Long id, HttpServletRequest request, HttpSession session) {

        System.out.println("=== 게시글 수정 폼 요청 ===");
        System.out.println("수정할 게시글 ID: " + id);

        // 1. 로그인 체크: 로그인하지 않은 사용자는 수정 불가
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            System.out.println("로그인하지 않은 사용자의 수정 시도");
            return "redirect:/login-form";
        }

        // 2. 수정할 게시글 조회
        Board board = boardRepository.findById(id);

        System.out.println("수정 대상 게시글: " + board.getTitle());
        System.out.println("게시글 작성자: " + board.getUser().getUsername());
        System.out.println("수정 요청자: " + sessionUser.getUsername());

        // 3. 권한 체크: 본인이 작성한 게시글만 수정 가능
        if (!board.isOwner(sessionUser.getId())) {
            System.out.println("수정 권한 없음: 다른 사용자의 게시글");
            throw new RuntimeException("수정 권한이 없습니다. 본인이 작성한 게시글만 수정할 수 있습니다.");
            // redirect 처리 및 예외 처리 추후 수정
        }

        // 4. 수정 폼에 기존 데이터 전달 (미리 채우기용)
        request.setAttribute("board", board);

        System.out.println("수정 폼 페이지로 이동");
        return "board/update-form";
    }

    // 게시글 수정 처리: Dirty Checking 활용
    @PostMapping("/board/{id}/update")
    public String update(@PathVariable("id") Long id, BoardRequest.UpdateDTO reqDTO,
                         HttpSession session, HttpServletRequest request) {

        System.out.println("=== 게시글 수정 요청 ===");
        System.out.println("수정할 게시글 ID: " + id);
        System.out.println("새 제목: " + reqDTO.getTitle());

        // 1. 로그인 체크
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            System.out.println("로그인하지 않은 사용자의 수정 시도");
            return "redirect:/login-form";
        }

        try {
            // 2. 입력 데이터 검증
            reqDTO.validate();

            // 3. 권한 체크를 위해 게시글 조회
            Board board = boardRepository.findById(id);

            if (!board.isOwner(sessionUser.getId())) {
                throw new RuntimeException("수정 권한이 없습니다");
            }

            System.out.println("권한 확인 완료: " + sessionUser.getUsername());

            // 4. Dirty Checking을 통한 수정 실행
            Board updatedBoard = boardRepository.updateById(id, reqDTO);

            System.out.println("=== 게시글 수정 완료 ===");
            System.out.println("수정된 게시글 ID: " + updatedBoard.getId());
            System.out.println("최종 제목: " + updatedBoard.getTitle());

            // 5. 수정 완료 후 해당 게시글 상세보기 페이지로 리다이렉트
            // PRG 패턴 적용으로 중복 수정 방지
            return "redirect:/board/" + id;

        } catch (IllegalArgumentException e) {
            // 검증 실패 시 에러 메시지와 함께 수정 폼으로 돌아가기
            System.out.println("게시글 수정 실패: " + e.getMessage());
            request.setAttribute("errorMessage", e.getMessage());

            // 수정 폼에 기존 데이터 다시 전달
            Board board = boardRepository.findById(id);
            request.setAttribute("board", board);

            return "board/update-form";

        } catch (RuntimeException e) {
            // 권한 없음 또는 기타 오류
            System.out.println("수정 실패: " + e.getMessage());
            return "redirect:/board/" + id + "?error=unauthorized";
        }
    }


    // 게시글 삭제: 권한 체크 포함
    @PostMapping("/board/{id}/delete")
    public String delete(@PathVariable("id") Long id, HttpSession session) {

        System.out.println("=== 게시글 삭제 요청 ===");
        System.out.println("삭제 대상 게시글 ID: " + id);

        // 1. 로그인 체크: 로그인하지 않은 사용자는 삭제 불가
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            System.out.println("로그인하지 않은 사용자의 삭제 시도");
            return "redirect:/login-form";
        }

        System.out.println("삭제 요청자: " + sessionUser.getUsername());

        try {
            // 2. 삭제할 게시글 조회 (권한 체크를 위해)
            Board board = boardRepository.findById(id);

            System.out.println("삭제 대상 게시글: " + board.getTitle());
            System.out.println("게시글 작성자: " + board.getUser().getUsername());

            // 3. 권한 체크: 본인이 작성한 게시글만 삭제 가능
            if (!board.isOwner(sessionUser.getId())) {
                System.out.println("삭제 권한 없음: 다른 사용자의 게시글");
                throw new RuntimeException("삭제 권한이 없습니다. 본인이 작성한 게시글만 삭제할 수 있습니다.");
            }

            // 4. 권한 확인 완료 후 삭제 실행
            boardRepository.deleteById(id);

            System.out.println("=== 게시글 삭제 완료 ===");
            System.out.println("삭제된 게시글 ID: " + id);

            // 5. 삭제 성공 시 메인 페이지로 리다이렉트
            return "redirect:/";

        } catch (IllegalArgumentException e) {
            // 존재하지 않는 게시글 삭제 시도
            System.out.println("삭제 실패: " + e.getMessage());
            return "redirect:/?error=notfound";

        } catch (RuntimeException e) {
            // 권한 없음 또는 기타 오류
            System.out.println("삭제 실패: " + e.getMessage());
            return "redirect:/board/" + id + "?error=unauthorized";
        }
    }


    // 게시글 작성 폼 페이지
    @GetMapping("/board/save-form")
    public String saveForm(HttpSession session) {
        // 로그인 체크: 로그인하지 않은 사용자는 게시글 작성 불가
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
            return "redirect:/login-form";
        }

        return "board/save-form";
    }

    // 게시글 저장: 로그인한 사용자와 연관관계 설정
    @PostMapping("/board/save")
    public String save(BoardRequest.SaveDTO reqDTO, HttpSession session, HttpServletRequest request) {

        System.out.println("=== 게시글 저장 요청 ===");

        // 1. 세션에서 로그인한 사용자 정보 가져오기
        User sessionUser = (User) session.getAttribute("sessionUser");

        // 2. 로그인 체크
        if (sessionUser == null) {
            System.out.println("로그인하지 않은 사용자의 게시글 작성 시도");
            return "redirect:/login-form";
        }

        System.out.println("작성자: " + sessionUser.getUsername());
        System.out.println("제목: " + reqDTO.getTitle());

        try {
            // 3. 입력 데이터 검증
            reqDTO.validate();

            // 4. DTO를 Entity로 변환 (로그인한 사용자 정보 포함)
            Board board = reqDTO.toEntity(sessionUser);

            System.out.println("=== Board 엔티티 생성 완료 ===");
            System.out.println("연관된 사용자: " + board.getUser().getUsername());

            // 5. Board 엔티티 영속화 (User와의 연관관계 포함)
            Board savedBoard = boardRepository.save(board);

            System.out.println("=== 게시글 저장 완료 ===");
            System.out.println("생성된 게시글 ID: " + savedBoard.getId());
            System.out.println("작성자 ID: " + savedBoard.getUser().getId());

            // 6. 저장 성공 시 메인 페이지로 리다이렉트
            return "redirect:/";

        } catch (IllegalArgumentException e) {
            // 검증 실패 시 에러 메시지와 함께 작성 폼으로 돌아가기
            System.out.println("게시글 저장 실패: " + e.getMessage());
            request.setAttribute("errorMessage", e.getMessage());
            return "board/save-form";
        }
    }


    // 메인 페이지: 게시글 목록 조회
    @GetMapping("/")
    public String index(HttpServletRequest request) {

        System.out.println("=== 메인 페이지 요청 ===");

        // 1. 게시글 목록 조회
        List<Board> boardList = boardRepository.findAll();

        // 2. 연관관계 동작 확인 (개발 시 확인용)
        System.out.println("=== 목록에서 작성자 정보 확인 ===");
        for (Board board : boardList) {
            System.out.println("게시글: " + board.getTitle() +
                    " | 작성자: " + board.getUser().getUsername());
        }

        // 3. 뷰에 데이터 전달
        request.setAttribute("boardList", boardList);

        return "index";
    }

    // 게시글 상세보기 - 연관관계를 통한 작성자 정보 함께 표시
    @GetMapping("/board/{id}")
    public String detail(@PathVariable(name = "id") Long id, HttpServletRequest request) {

        // 1. 게시글 조회 (User 연관관계 포함)
        Board board = boardRepository.findById(id);

        // 2. 연관관계 동작 확인 (선택사항 - 개발 시 확인용)
        System.out.println("=== 연관관계 동작 확인 ===");
        System.out.println("게시글 제목: " + board.getTitle());
        System.out.println("작성자명: " + board.getUser().getUsername());
        System.out.println("작성자 이메일: " + board.getUser().getEmail());

        // 3. 뷰에 데이터 전달
        request.setAttribute("board", board);

        return "board/detail";
    }


}

