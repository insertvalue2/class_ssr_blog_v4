package com.tenco.blog.board;

import com.tenco.blog.user.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

@Import(BoardRepository.class)
@DataJpaTest
public class BoardRepositoryTest {

    @Autowired
    private BoardRepository boardRepository;

    @Test
    public void updateById_Dirty_Checking_테스트() {
        // given: 수정할 게시글 ID와 수정 데이터 준비
        Long id = 1L;

        BoardRequest.UpdateDTO updateDTO = new BoardRequest.UpdateDTO();
        updateDTO.setTitle("수정된 제목");
        updateDTO.setContent("수정된 내용입니다");

        // 수정 전 게시글 상태 확인
        Board beforeUpdate = boardRepository.findById(id);
        String originalTitle = beforeUpdate.getTitle();
        String originalContent = beforeUpdate.getContent();

        System.out.println("=== 수정 전 상태 ===");
        System.out.println("원본 제목: " + originalTitle);
        System.out.println("원본 내용: " + originalContent);

        // when: Dirty Checking을 통한 게시글 수정
        Board updatedBoard = boardRepository.updateById(id, updateDTO);

        // then: 수정 결과 검증
        System.out.println("=== 수정 후 상태 ===");
        System.out.println("수정된 제목: " + updatedBoard.getTitle());
        System.out.println("수정된 내용: " + updatedBoard.getContent());

        // 1. 수정된 데이터가 올바르게 적용되었는지 확인
        Assertions.assertThat(updatedBoard.getTitle()).isEqualTo("수정된 제목");
        Assertions.assertThat(updatedBoard.getContent()).isEqualTo("수정된 내용입니다");

        // 2. 원본 데이터와 다른지 확인
        Assertions.assertThat(updatedBoard.getTitle()).isNotEqualTo(originalTitle);
        Assertions.assertThat(updatedBoard.getContent()).isNotEqualTo(originalContent);

        // 3. ID와 생성시간은 변경되지 않았는지 확인
        Assertions.assertThat(updatedBoard.getId()).isEqualTo(id);
        Assertions.assertThat(updatedBoard.getCreatedAt()).isEqualTo(beforeUpdate.getCreatedAt());

        // 4. 연관관계(User)는 변경되지 않았는지 확인
        Assertions.assertThat(updatedBoard.getUser().getId()).isEqualTo(beforeUpdate.getUser().getId());

        System.out.println("Dirty Checking 테스트 완료: 변경된 필드만 자동 UPDATE");
    }

    @Test
    public void deleteById_JPQL_삭제_테스트() {
        // given: 삭제할 게시글 ID 준비
        // data.sql에 의해 게시글들이 존재 (ID: 1, 2, 3, ...)
        Long deleteId = 1L;

        // 삭제 전 전체 게시글 수 확인
        List<Board> beforeDelete = boardRepository.findAll();
        int beforeCount = beforeDelete.size();

        System.out.println("=== 삭제 전 상태 ===");
        System.out.println("전체 게시글 수: " + beforeCount);

        // when: JPQL DELETE 쿼리로 게시글 삭제
        boardRepository.deleteById(deleteId);

        // then: 삭제 결과 검증
        List<Board> afterDelete = boardRepository.findAll();
        int afterCount = afterDelete.size();

        System.out.println("=== 삭제 후 상태 ===");
        System.out.println("전체 게시글 수: " + afterCount);

        // 1. 전체 게시글 수가 1개 감소했는지 확인
        Assertions.assertThat(afterCount).isEqualTo(beforeCount - 1);

        // 2. 삭제된 게시글이 목록에서 제거되었는지 확인
        boolean isDeleted = afterDelete.stream()
                .noneMatch(board -> board.getId().equals(deleteId));
        Assertions.assertThat(isDeleted).isTrue();

        System.out.println("JPQL DELETE 테스트 완료: 게시글이 성공적으로 삭제됨");
    }

    @Test
    public void save_연관관계_포함_게시글_저장_테스트() {
        // given: User 객체 생성 (실제로는 세션에서 가져온 사용자)
        User user = User.builder()
                .id(1L)  // 기존 사용자 ID
                .username("testuser")
                .email("test@email.com")
                .build();

        // Board 엔티티 생성 (User와 연관관계 설정)
        Board board = Board.builder()
                .title("테스트 게시글")
                .content("테스트 내용입니다")
                .user(user)  // 연관관계 설정
                .build();

        // 저장 전 상태 확인
        Assertions.assertThat(board.getId()).isNull();
        System.out.println("저장 전 Board: " + board.getTitle());
        System.out.println("작성자: " + board.getUser().getUsername());

        // when: 게시글 저장 (연관관계 포함)
        Board savedBoard = boardRepository.save(board);

        // then: 저장 결과 검증
        // 1. 자동 생성된 ID 확인
        Assertions.assertThat(savedBoard.getId()).isNotNull();
        Assertions.assertThat(savedBoard.getId()).isGreaterThan(0);

        // 2. 입력한 데이터가 올바르게 저장되었는지 확인
        Assertions.assertThat(savedBoard.getTitle()).isEqualTo("테스트 게시글");
        Assertions.assertThat(savedBoard.getContent()).isEqualTo("테스트 내용입니다");

        // 3. 연관관계가 올바르게 저장되었는지 확인
        Assertions.assertThat(savedBoard.getUser()).isNotNull();
        Assertions.assertThat(savedBoard.getUser().getUsername()).isEqualTo("testuser");

        // 4. 자동으로 생성된 생성시간 확인
        Assertions.assertThat(savedBoard.getCreatedAt()).isNotNull();

        System.out.println("저장 후 Board ID: " + savedBoard.getId());
        System.out.println("연관된 User: " + savedBoard.getUser().getUsername());

        // 5. 원본 객체와 반환된 객체가 동일한 참조인지 확인
        Assertions.assertThat(board).isSameAs(savedBoard);
    }

}