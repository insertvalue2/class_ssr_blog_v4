package com.tenco.blog.board;

/**
 * Persistence Context 활용한 Repository 만들기
 * Repository란
 * "저장소", "보관소", "창고"를 의미합니다.
 * 소프트웨어에서는 데이터를 저장하고 관리하는 곳을
 * 추상화한 개념입니다.
 */


import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@RequiredArgsConstructor
@Repository
public class BoardPersistRepository {

    // EntityManager: JPA의 핵심 인터페이스
    // 영속성 컨텍스트를 관리하고 엔티티의 생명주기를 제어
    private final EntityManager em;


    // Dirty Checking을 활용한 게시글 수정
    @Transactional
    public void updateById(Long id, BoardRequest.UpdateDTO reqDTO) {
        // 1. 수정할 엔티티를 영속 상태로 조회
        Board board = em.find(Board.class, id);

        // 2. 엔티티 존재 여부 확인
        if (board == null) {
            throw new IllegalArgumentException("수정할 게시글을 찾을 수 없습니다. ID: " + id);
        }

        // 3. 영속 상태 엔티티의 값 변경 (Dirty Checking 시작)
        board.update(reqDTO);

        // 4. persist() 호출 불필요!
        // 트랜잭션 커밋 시점에 영속성 컨텍스트가 자동으로 변경 감지
        // 변경된 필드만 UPDATE 쿼리 자동 생성 및 실행

        // Dirty Checking의 장점:
        // - 개발자가 UPDATE 쿼리 작성할 필요 없음
        // - 변경된 필드만 자동으로 UPDATE (성능 최적화)
        // - 영속성 컨텍스트가 엔티티 상태 자동 관리
        // - 1차 캐시의 엔티티 정보도 자동 갱신
    }

    // 더 안전한 수정 메서드 (반환값으로 성공 여부 확인)
    @Transactional
    public Board updateByIdSafely(Long id, BoardRequest.UpdateDTO reqDTO) {
        Board board = em.find(Board.class, id);

        if (board != null) {
            board.update(reqDTO);
            return board;  // 수정된 영속 엔티티 반환
        }

        return null;  // 수정할 엔티티 없음
    }


    // 영속성 컨텍스트를 활용한 안전한 삭제
    @Transactional
    public void deleteById(Long id) {
        // 1. 먼저 삭제할 엔티티를 영속 상태로 조회
        Board board = em.find(Board.class, id);

        // 2. 엔티티 존재 여부 확인 (안전한 삭제)
        if (board == null) {
            throw new IllegalArgumentException("삭제할 게시글을 찾을 수 없습니다. ID: " + id);
        }

        // 3. 영속 상태의 엔티티를 삭제 상태로 변경
        em.remove(board);

        // 삭제 과정:
        // - board 엔티티가 영속(Managed) → 삭제(Removed) 상태로 변경
        // - 1차 캐시에서 해당 엔티티 제거
        // - 트랜잭션 커밋 시점에 DELETE SQL 자동 실행
        // - 연관관계 처리 자동 수행 (CASCADE 설정 시)

        // V1과의 차이점:
        // V1: 직접 DELETE SQL 작성, 존재 여부 수동 확인
        // V2: 영속성 컨텍스트가 엔티티 생명주기 자동 관리

//        JPQL 로 작업해본 코드
//        Query query = em.createQuery("delete from Board b where b.id = :id");
//        query.setParameter("id", id);
//        query.executeUpdate();
    }

    // ** HTTP 요청 하나 = 하나의 트랜잭션 = 하나의 EntityManager **
    // 기본키로 게시글 단건 조회 (1차 캐시 활용)
    public Board findById(Long id) {
        // em.find(): 기본키를 사용한 최적화된 조회 방법
        // 1차 캐시 활용: 같은 트랜잭션 내에서 동일 ID 조회시 DB 접근 없이 캐시에서 반환
        Board board = em.find(Board.class, id);

        // find()의 특징:
        // 1. 기본키로만 조회 가능
        // 2. 1차 캐시에서 먼저 찾기 시도
        // 3. 없으면 DB에서 조회 후 1차 캐시에 저장
        // 4. 결과가 없으면 null 반환 (예외 발생 안함)
        // 5. 영속 상태 엔티티 반환

        return board;
        // ** 응답 완료 → 트랜잭션 종료 → EntityManager 소멸 → 1차 캐시 소멸 **
    }

    // JPQL을 사용한 조회 방법 (비교용 - 실제로는 find() 권장)
    public Board findByIdWithJPQL(Long id) {
        String jpql = "SELECT b FROM Board b WHERE b.id = :id";

        try {
            return em.createQuery(jpql, Board.class)
                    .setParameter("id", id)
                    .getSingleResult();  // 결과가 없으면 NoResultException 발생
        } catch (Exception e) {
            return null;
        }

        // JPQL 단점:
        // 1. 캐시 확인 없이 바로 DB에 쿼리 실행
        // 2. DB 결과를 엔티티로 변환
        // 3. 1차 캐시에 같은 ID 엔티티가 있는지 확인
        // 4. 있으면 → 캐시의 기존 인스턴스 반환 (새로 만든 객체는 버림)
        // 5. 없으면 → 새 인스턴스를 캐시에 저장하고 반환
    }

    // JPQL을 사용한 게시글 목록 조회
    public List<Board> findAll() {
        // JPQL: 엔티티 객체를 대상으로 하는 객체지향 쿼리
        // Board는 엔티티 클래스명, b는 별칭
        // 테이블명(board_tb)이 아닌 엔티티명(Board) 사용
        String jpql = "SELECT b FROM Board b ORDER BY b.createdAt DESC";

        // createQuery(): JPQL 쿼리 생성
        // 두 번째 매개변수로 반환 타입 지정 (타입 안전성 확보)
        return em.createQuery(jpql, Board.class)
                .getResultList();  // List<Board> 반환

        // V1과의 차이점:
        // V1: createNativeQuery("SELECT * FROM board_tb ORDER BY id DESC")
        // V2: createQuery("SELECT b FROM Board b ORDER BY b.createdAt DESC")
        // - 테이블명 → 엔티티명
        // - 컬럼명 → 필드명
        // - SQL → JPQL
    }


    // 게시글 저장: Persistence Context를 활용한 엔티티 영속화
    @Transactional
    public Board save(Board board) {
        // 1. 매개변수로 받은 board는 비영속(Transient) 상태
        //    - 아직 영속성 컨텍스트에 관리되지 않는 상태
        //    - 데이터베이스와 연관 없는 순수 Java 객체

        // 2. em.persist(board): 엔티티를 영속성 컨텍스트에 저장
        //    - board 객체가 영속(Managed) 상태로 변경됨
        //    - 영속성 컨텍스트가 엔티티를 관리 시작
        //    - 아직 실제 INSERT 쿼리는 실행되지 않음 (지연 쓰기)
        em.persist(board);

        // 3. 트랜잭션 커밋 시점에 실제 INSERT 쿼리 실행
        //    - @Transactional 메서드 종료 시 자동 커밋
        //    - 이때 영속성 컨텍스트의 변경사항이 DB에 반영됨
        //    - board 객체의 id 필드에 자동 생성된 값이 설정됨

        // 4. 영속 상태의 board 객체 반환
        //    - 이제 board는 영속성 컨텍스트에서 관리되는 엔티티
        //    - 자동으로 생성된 id 값을 포함
        return board;
    }
}