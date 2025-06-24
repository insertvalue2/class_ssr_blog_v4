package com.tenco.blog.board;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class BoardRepository {
    private final EntityManager em;


    // Dirty Checking을 활용한 게시글 수정
    @Transactional
    public Board updateById(Long id, BoardRequest.UpdateDTO reqDTO) {
        // 1. 수정할 게시글을 영속 상태로 조회
        Board board = findById(id);  // 영속성 컨텍스트에서 관리되는 엔티티

        System.out.println("=== Dirty Checking 수정 시작 ===");
        System.out.println("수정 전 제목: " + board.getTitle());
        System.out.println("수정 전 내용: " + board.getContent());

        // 2. 영속 상태 엔티티의 값 변경 (Dirty Checking 시작)
        // Board 엔티티의 update() 메서드 호출
        board.update(reqDTO);

        System.out.println("=== 엔티티 값 변경 완료 ===");
        System.out.println("수정 후 제목: " + board.getTitle());
        System.out.println("수정 후 내용: " + board.getContent());

        // 3. persist() 호출 불필요!
        // 트랜잭션 커밋 시점에 영속성 컨텍스트가 자동으로 변경 감지
        // 변경된 필드만 UPDATE 쿼리 자동 생성 및 실행

        return board;

        // Dirty Checking의 동작 과정:
        // 1. 영속성 컨텍스트가 엔티티 최초 상태를 스냅샷으로 보관
        // 2. 필드 값 변경 시 현재 상태와 스냅샷 비교
        // 3. 트랜잭션 커밋 시점에 변경된 필드만 UPDATE 쿼리 자동 생성
        // 4. UPDATE board_tb SET title=?, content=? WHERE id=?
    }

    // JPQL을 사용한 게시글 삭제
    @Transactional
    public void deleteById(Long id) {
        // 1. JPQL DELETE 쿼리 작성
        // 엔티티명(Board) 사용, 테이블명(board_tb) 아님
        String jpql = "DELETE FROM Board b WHERE b.id = :id";

        Query query = em.createQuery(jpql);
        query.setParameter("id", id);

        // 2. DELETE 쿼리 실행
        int deletedCount = query.executeUpdate();

        System.out.println("=== JPQL DELETE 실행 ===");
        System.out.println("삭제 대상 ID: " + id);
        System.out.println("삭제된 행 수: " + deletedCount);

        // 3. 삭제 결과 확인
        if (deletedCount == 0) {
            throw new IllegalArgumentException("삭제할 게시글을 찾을 수 없습니다. ID: " + id);
        }

        // executeUpdate() 특징:
        // - INSERT, UPDATE, DELETE 쿼리에 사용
        // - 영향받은 행의 수를 반환
        // - 즉시 데이터베이스에 반영됨 (1차 캐시 우회)
    }

    // 권장!
    // 안전한 삭제를 위한 대안 메서드 (em.remove 사용)
    @Transactional
    public void deleteByIdSafely(Long id) {
        // 1. 먼저 삭제할 엔티티를 영속 상태로 조회
        Board board = em.find(Board.class, id);

        // 2. 엔티티 존재 여부 확인
        if (board == null) {
            throw new IllegalArgumentException("삭제할 게시글을 찾을 수 없습니다. ID: " + id);
        }

        // 3. 영속 상태의 엔티티를 삭제 상태로 변경
        em.remove(board);

        System.out.println("=== em.remove() 삭제 실행 ===");
        System.out.println("삭제된 게시글: " + board.getTitle());
        System.out.println("작성자: " + board.getUser().getUsername());

        // em.remove()의 장점:
        // - 영속성 컨텍스트에서 관리되는 엔티티 삭제
        // - 1차 캐시에서도 자동 제거
        // - 연관관계 처리 자동 수행
    }



    // 게시글 저장: User와 연관관계를 가진 Board 엔티티 영속화
    @Transactional
    public Board save(Board board) {
        // 비영속 상태의 Board 엔티티를 영속성 컨텍스트에 저장
        // Board의 User 연관관계도 함께 처리됨
        em.persist(board);

        // persist() 후 board 객체는 영속 상태가 됨
        // 트랜잭션 커밋 시점에 실제 INSERT 쿼리 실행
        // 자동 생성된 ID와 생성시간이 board 객체에 설정됨
        return board;
    }

    // 게시글 목록 조회 - 최신순 정렬
    public List<Board> findAll() {
        // JPQL로 Board 엔티티 목록을 최신순으로 조회
        // ORDER BY b.id DESC: 최신 게시글이 위에 오도록 정렬
        String jpql = "SELECT b FROM Board b ORDER BY b.id DESC";

        Query query = em.createQuery(jpql, Board.class);
        List<Board> boardList = query.getResultList();

        System.out.println("조회된 게시글 수: " + boardList.size());
        return boardList;
    }


    // 게시글 단건 조회 - 연관관계 포함
    public Board findById(Long id) {
        // EntityManager의 find() 메서드로 기본키 조회
        Board board = em.find(Board.class, id);

        // 조회 결과 검증
        if (board == null) {
            throw new RuntimeException("게시글을 찾을 수 없습니다. ID: " + id);
        }

        return board;
        // 반환된 Board 객체는 연관된 User 정보에도 접근 가능
    }
}