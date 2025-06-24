package com.tenco.blog.board;

import com.tenco.blog._core.errors.exception.Exception403;
import com.tenco.blog.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BoardController {

    private static final Logger log = LoggerFactory.getLogger(BoardController.class);

    private final BoardRepository boardRepository;

    // 게시글 수정 폼 페이지: 기존 데이터로 폼 미리 채우기
    @GetMapping("/board/{id}/update-form")
    public String updateForm(@PathVariable("id") Long id, HttpServletRequest request, HttpSession session) {

        log.info("게시글 수정 폼 요청 - ID: {}", id);

        User sessionUser = (User) session.getAttribute("sessionUser");

        // 수정할 게시글 조회 (Exception404 자동 처리됨)
        Board board = boardRepository.findById(id);

        log.info("게시글 수정 폼 조회 - 제목: {}, 작성자: {}, 요청자: {}",
                board.getTitle(), board.getUser().getUsername(), sessionUser.getUsername());

        // 권한 체크: 본인이 작성한 게시글만 수정 가능
        if (!board.isOwner(sessionUser.getId())) {
            log.warn("게시글 수정 권한 없음 - 게시글 ID: {}, 작성자: {}, 요청자: {}",
                    id, board.getUser().getUsername(), sessionUser.getUsername());
            // @ControllerAdvice에서 403 에러 페이지 처리됨
            throw new Exception403("본인이 작성한 게시글만 수정할 수 있습니다.");
        }

        // 수정 폼에 기존 데이터 전달 (미리 채우기용)
        request.setAttribute("board", board);

        log.info("게시글 수정 폼 페이지 이동 완료");
        return "board/update-form";
    }

    // 게시글 수정 처리: Dirty Checking 활용
    @PostMapping("/board/{id}/update")
    public String update(@PathVariable("id") Long id, BoardRequest.UpdateDTO reqDTO,
                         HttpSession session, HttpServletRequest request) {

        log.info("게시글 수정 요청 - ID: {}, 새 제목: {}", id, reqDTO.getTitle());

        User sessionUser = (User) session.getAttribute("sessionUser");

        // 입력 데이터 검증 (Exception400 자동 처리됨)
        reqDTO.validate();

        // 권한 체크를 위해 게시글 조회
        Board board = boardRepository.findById(id);

        if (!board.isOwner(sessionUser.getId())) {
            log.warn("게시글 수정 권한 없음 - 게시글 ID: {}, 요청자: {}", id, sessionUser.getUsername());
            throw new Exception403("본인이 작성한 게시글만 수정할 수 있습니다.");
        }

        // Dirty Checking을 통한 수정 실행
        Board updatedBoard = boardRepository.updateById(id, reqDTO);

        log.info("게시글 수정 완료 - ID: {}, 제목: {}", updatedBoard.getId(), updatedBoard.getTitle());

        // 수정 완료 후 해당 게시글 상세보기 페이지로 리다이렉트
        // PRG 패턴 적용으로 중복 수정 방지
        return "redirect:/board/" + id;
    }

    // 게시글 삭제: 권한 체크 포함
    @PostMapping("/board/{id}/delete")
    public String delete(@PathVariable("id") Long id, HttpSession session) {

        log.info("게시글 삭제 요청 - ID: {}", id);

        User sessionUser = (User) session.getAttribute("sessionUser");

        // 삭제할 게시글 조회 (권한 체크를 위해)
        Board board = boardRepository.findById(id);

        log.info("삭제 대상 게시글 - 제목: {}, 작성자: {}, 요청자: {}",
                board.getTitle(), board.getUser().getUsername(), sessionUser.getUsername());

        // 권한 체크: 본인이 작성한 게시글만 삭제 가능
        if (!board.isOwner(sessionUser.getId())) {
            log.warn("게시글 삭제 권한 없음 - 게시글 ID: {}, 요청자: {}", id, sessionUser.getUsername());
            throw new Exception403("본인이 작성한 게시글만 삭제할 수 있습니다.");
        }

        // 권한 확인 완료 후 삭제 실행
        boardRepository.deleteById(id);

        log.info("게시글 삭제 완료 - ID: {}", id);

        // 삭제 성공 시 메인 페이지로 리다이렉트
        return "redirect:/";
    }

    // 게시글 작성 폼 페이지
    @GetMapping("/board/save-form")
    public String saveForm(HttpSession session) {

        log.info("게시글 작성 폼 요청");

        return "board/save-form";
    }

    // 게시글 저장: 로그인한 사용자와 연관관계 설정
    @PostMapping("/board/save")
    public String save(BoardRequest.SaveDTO reqDTO, HttpSession session) {

        log.info("게시글 저장 요청 - 제목: {}", reqDTO.getTitle());

        // 세션에서 로그인한 사용자 정보 가져오기
        User sessionUser = (User) session.getAttribute("sessionUser");

        // 입력 데이터 검증 (Exception400 자동 처리됨)
        reqDTO.validate();

        // DTO를 Entity로 변환 (로그인한 사용자 정보 포함)
        Board board = reqDTO.toEntity(sessionUser);

        log.info("Board 엔티티 생성 완료 - 작성자: {}", board.getUser().getUsername());

        // Board 엔티티 영속화 (User와의 연관관계 포함)
        Board savedBoard = boardRepository.save(board);

        log.info("게시글 저장 완료 - ID: {}, 제목: {}", savedBoard.getId(), savedBoard.getTitle());

        // 저장 성공 시 메인 페이지로 리다이렉트
        return "redirect:/";
    }

    // 메인 페이지: 게시글 목록 조회
    @GetMapping("/")
    public String index(HttpServletRequest request) {

        log.info("메인 페이지 요청");

        // 게시글 목록 조회
        List<Board> boardList = boardRepository.findAll();

        log.info("게시글 목록 조회 완료 - 총 {}개", boardList.size());

        // 뷰에 데이터 전달
        request.setAttribute("boardList", boardList);

        return "index";
    }

    // 게시글 상세보기 - 연관관계를 통한 작성자 정보 함께 표시
    @GetMapping("/board/{id}")
    public String detail(@PathVariable("id") Long id, HttpServletRequest request) {

        log.info("게시글 상세보기 요청 - ID: {}", id);

        // 게시글 조회 (User 연관관계 포함)
        // Exception404 자동 처리됨
        Board board = boardRepository.findById(id);

        log.info("게시글 상세보기 조회 완료 - 제목: {}, 작성자: {}",
                board.getTitle(), board.getUser().getUsername());

        // 뷰에 데이터 전달
        request.setAttribute("board", board);

        return "board/detail";
    }
}