package com.tenco.blog.board;

import com.tenco.blog._core.errors.exception.Exception404;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class BoardRepository {

    private static final Logger log = LoggerFactory.getLogger(BoardRepository.class);

    private final EntityManager em;

    // 게시글 저장: User와 연관관계를 가진 Board 엔티티 영속화
    @Transactional
    public Board save(Board board) {
        log.info("게시글 저장 시작 - 제목: {}, 작성자: {}", board.getTitle(), board.getUser().getUsername());

        // 비영속 상태의 Board 엔티티를 영속성 컨텍스트에 저장
        em.persist(board);

        log.info("게시글 영속화 완료 - ID: {}", board.getId());
        return board;
    }

    // 게시글 단건 조회 - 커스텀 예외 사용
    public Board findById(Long id) {
        Board board = em.find(Board.class, id);

        if (board == null) {
            log.warn("존재하지 않는 게시글 조회 시도 - ID: {}", id);
            // @ControllerAdvice에서 404 에러 페이지 처리됨
            throw new Exception404("요청하신 게시글을 찾을 수 없습니다. ID: " + id);
        }

        log.debug("게시글 조회 완료 - ID: {}, 제목: {}", board.getId(), board.getTitle());
        return board;
    }

    // 게시글 목록 조회
    public List<Board> findAll() {
        String jpql = "SELECT b FROM Board b ORDER BY b.id DESC";
        Query query = em.createQuery(jpql, Board.class);
        List<Board> boardList = query.getResultList();

        log.info("게시글 목록 조회 완료 - 총 {}개", boardList.size());
        return boardList;
    }

    // Dirty Checking을 활용한 게시글 수정
    @Transactional
    public Board updateById(Long id, BoardRequest.UpdateDTO reqDTO) {
        log.info("게시글 수정 시작 - ID: {}", id);

        // 1. 수정할 게시글을 영속 상태로 조회 (Exception404 자동 처리)
        Board board = findById(id);

        log.info("수정 전 상태 - 제목: {}, 내용 길이: {}자", board.getTitle(), board.getContent().length());

        // 2. 영속 상태 엔티티의 값 변경 (Dirty Checking 시작)
        board.update(reqDTO);

        log.info("수정 후 상태 - 제목: {}, 내용 길이: {}자", board.getTitle(), board.getContent().length());
        log.info("게시글 수정 완료 - ID: {}", id);

        return board;
    }

    // JPQL을 사용한 게시글 삭제
    @Transactional
    public void deleteById(Long id) {
        log.info("게시글 삭제 시작 - ID: {}", id);

        // 1. JPQL DELETE 쿼리 작성
        String jpql = "DELETE FROM Board b WHERE b.id = :id";

        Query query = em.createQuery(jpql);
        query.setParameter("id", id);

        // 2. DELETE 쿼리 실행
        int deletedCount = query.executeUpdate();

        // 3. 삭제 결과 확인
        if (deletedCount == 0) {
            log.warn("삭제할 게시글이 존재하지 않음 - ID: {}", id);
            throw new Exception404("삭제할 게시글을 찾을 수 없습니다. ID: " + id);
        }

        log.info("게시글 삭제 완료 - ID: {}, 삭제된 행 수: {}", id, deletedCount);
    }
}