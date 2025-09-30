package com.crodrigo47.trelloBackend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.crodrigo47.trelloBackend.exception.BoardNotFoundException;
import com.crodrigo47.trelloBackend.helper.Builders;
import com.crodrigo47.trelloBackend.model.Board;
import com.crodrigo47.trelloBackend.model.Task;
import com.crodrigo47.trelloBackend.model.User;
import com.crodrigo47.trelloBackend.repository.BoardRepository;
import com.crodrigo47.trelloBackend.repository.TaskRepository;
import com.crodrigo47.trelloBackend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Mock
    BoardRepository boardRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    TaskRepository taskRepository;

    @InjectMocks
    BoardService boardService;

    @Test
    void getAllBoardsForCurrentUser_returnsList() {
        User user = Builders.buildUserWithId("bob", 1L);
        when(boardRepository.findByUsersId(user.getId()))
                .thenReturn(List.of(
                        Builders.buildBoardWithId("Diseño", 1L, user),
                        Builders.buildBoardWithId("Programación", 2L, user)
                ));

        List<Board> boards = boardService.getAllBoardsForCurrentUser(user);

        assertThat(boards).hasSize(2);
        verify(boardRepository).findByUsersId(user.getId());
    }

    @Test
    void getBoardById_returnsBoard() {
        User user = Builders.buildUserWithId("bob", 1L);
        Board board = Builders.buildBoardWithId("Diseño", 1L, user);
        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));

        Board result = boardService.getBoardById(1L, user);

        assertThat(result.getName()).isEqualTo("Diseño");
    }

    @Test
    void getBoardById_boardNotFound_throwsException() {
        User user = Builders.buildUserWithId("bob", 1L);
        when(boardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> boardService.getBoardById(1L, user))
                .isInstanceOf(BoardNotFoundException.class);
    }

    @Test
    void createBoard_returnsBoard() {
        User user = Builders.buildUserWithId("bob", 1L);
        Board board = Builders.buildBoard("Diseño", user);
        when(boardRepository.save(any(Board.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Board result = boardService.createBoard(board, user);

        assertThat(result.getCreatedBy()).isEqualTo(user);
        assertThat(result.getUsers()).contains(user);
    }

    @Test
    void updateBoard_updatesBoard() {
        User user = Builders.buildUserWithId("bob", 1L);
        Board existing = Builders.buildBoardWithId("Old Name", 1L, user);
        Board updated = Builders.buildBoardWithId("New Name", 1L, user);

        when(boardRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(boardRepository.save(any(Board.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Board result = boardService.updateBoard(updated, user);

        assertThat(result.getName()).isEqualTo("New Name");
    }

    @Test
    void updateBoard_notCreator_throwsException() {
        User creator = Builders.buildUserWithId("bob", 1L);
        User other = Builders.buildUserWithId("alice", 2L);
        Board existing = Builders.buildBoardWithId("Board", 1L, creator);

        when(boardRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> boardService.updateBoard(existing, other))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void deleteBoard_callsRepository() {
        User user = Builders.buildUserWithId("bob", 1L);
        Board board = Builders.buildBoardWithId("Board", 1L, user);

        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));

        boardService.deleteBoard(1L, user);

        verify(boardRepository).delete(board);
    }

    @Test
void getTasksFromBoard_returnTasks() {
    User user = Builders.buildUser("bob");
    Board board = Builders.buildBoardWithId("Diseño", 1L, user);
    Task task1 = Builders.buildTaskWithId("Tarea1", 10L, board, user, user);
    Task task2 = Builders.buildTaskWithId("Tarea2", 11L, board, user, user);
    board.addTask(task1);
    board.addTask(task2);

    when(boardRepository.findById(1L)).thenReturn(Optional.of(board));

    var tasks = boardService.getTasksFromBoard(1L, user);

    assertThat(tasks).containsExactlyInAnyOrder(task1, task2);
}

@Test
void getTasksFromBoard_boardNotFound_throwException() {
    User user = Builders.buildUser("bob");
    when(boardRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> boardService.getTasksFromBoard(1L, user))
            .isInstanceOf(BoardNotFoundException.class);
}

@Test
void getUsersFromBoard_returnUsers() {
    User creator = Builders.buildUser("bob");
    User user1 = Builders.buildUserWithId("alice", 2L);
    User user2 = Builders.buildUserWithId("john", 3L);
    Board board = Builders.buildBoardWithId("Diseño", 1L, creator);
    board.addUser(creator);
    board.addUser(user1);
    board.addUser(user2);

    when(boardRepository.findById(1L)).thenReturn(Optional.of(board));

    var users = boardService.getUsersFromBoard(1L, creator);

    assertThat(users).containsExactlyInAnyOrder(creator, user1, user2);
}

@Test
void getUsersFromBoard_boardNotFound_throwException() {
    User user = Builders.buildUser("bob");
    when(boardRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> boardService.getUsersFromBoard(1L, user))
            .isInstanceOf(BoardNotFoundException.class);
}

@Test
void searchBoardsByName_returnBoardsMatchingName() {
    User user = Builders.buildUser("bob");
    Board board1 = Builders.buildBoardWithId("Diseño Web", 1L, user);
    Board board2 = Builders.buildBoardWithId("Diseño UX", 2L, user);

    when(boardRepository.findByUsersIdAndNameContainingIgnoreCase(user.getId(), "Diseño"))
            .thenReturn(List.of(board1, board2));

    var result = boardService.searchBoardsByName(user, "Diseño");

    assertThat(result).containsExactlyInAnyOrder(board1, board2);
}

@Test
void searchBoardsByName_noMatch_returnsEmptyList() {
    User user = Builders.buildUser("bob");

    when(boardRepository.findByUsersIdAndNameContainingIgnoreCase(user.getId(), "NoExiste"))
            .thenReturn(List.of());

    var result = boardService.searchBoardsByName(user, "NoExiste");

    assertThat(result).isEmpty();
}


}

