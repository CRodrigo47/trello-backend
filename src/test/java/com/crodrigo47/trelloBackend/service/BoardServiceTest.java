package com.crodrigo47.trelloBackend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.crodrigo47.trelloBackend.exception.BoardNotFoundException;
import com.crodrigo47.trelloBackend.exception.UserNotFoundException;
import com.crodrigo47.trelloBackend.helper.Builders;
import com.crodrigo47.trelloBackend.model.Board;
import com.crodrigo47.trelloBackend.model.Task;
import com.crodrigo47.trelloBackend.model.User;
import com.crodrigo47.trelloBackend.repository.BoardRepository;
import com.crodrigo47.trelloBackend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Mock
    BoardRepository boardRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    TaskService taskService;

    @InjectMocks
    BoardService boardService;

    @Test
    void getAllBoards_returnList() {
        when(boardRepository.findAll()).thenReturn(List.of(Builders.buildBoard("Diseño"), Builders.buildBoard("Programación")));

        var result = boardService.getAllBoards();

        assertThat(result).hasSize(2);
        verify(boardRepository).findAll();
    }

    @Test
    void getBoardById_returnBoard() {
        when(boardRepository.findById(1L)).thenReturn(Optional.of(Builders.buildBoardWithId("Diseño", 1L)));

        var result = boardService.getBoardById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Diseño");
    }

    @Test
    void createBoard_returnBoard() {
        when(boardRepository.save(any(Board.class))).thenReturn(Builders.buildBoardWithId("Diseño", 1L));

        var result = boardService.createBoard(Builders.buildBoard("Diseño"));

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Diseño");
    }

    @Test
    void updateBoard_returnBoard() {
        Board boardToSave = Builders.buildBoardWithId("Programación", 2L);

        when(boardRepository.save(any(Board.class))).thenReturn(boardToSave);

        var result = boardService.updateBoard(boardToSave);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getName()).isEqualTo("Programación");
    }

    @Test
    void deleteBoard_callsRepository() {
        boardService.deleteBoard(5L);

        verify(boardRepository).deleteById(5L);
    }

    @Test
    void addTaskToBoard_returnBoardWithTask() {
        Board board = Builders.buildBoardWithId("Diseño", 1L);
        Task task = Builders.buildTaskWithId("Tarea", 10L, board, Builders.buildUser("bob"));

        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));
        when(boardRepository.save(any(Board.class))).thenReturn(board);

        var result = boardService.addTaskToBoard(1L, task);

        assertThat(result.getTasks()).contains(task);
        verify(boardRepository).save(board);
    }

    @Test
    void addTaskToBoard_notFound_throwException() {
        when(boardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> boardService.addTaskToBoard(1L, new Task()))
                .isInstanceOf(BoardNotFoundException.class);
    }

    @Test
    void addUserToBoard_returnBoardWithUser() {
        Board board = Builders.buildBoardWithId("Programación", 1L);
        User user = Builders.buildUserWithId("alice", 2L);

        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(boardRepository.save(any(Board.class))).thenReturn(board);

        var result = boardService.addUserToBoard(1L, 2L);

        assertThat(result.getUsers()).contains(user);
        verify(boardRepository).save(board);
    }

    @Test
    void addUserToBoard_userNotFound_throwException() {
        Board board = Builders.buildBoardWithId("Diseño", 1L);
        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> boardService.addUserToBoard(1L, 2L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getTasksFromBoard_returnTasks() {
        Task task = Builders.buildTaskWithId("Tarea", 3L, Builders.buildBoard("Diseño"), Builders.buildUser("bob"));
        Board board = Builders.buildBoardWithId("Diseño", 1L);
        board.addTask(task);

        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));

        Set<Task> result = boardService.getTasksFromBoard(1L);

        assertThat(result).contains(task);
    }

    @Test
    void getUsersFromBoard_returnUsers() {
        User user = Builders.buildUserWithId("alice", 2L);
        Board board = Builders.buildBoardWithId("Diseño", 1L);
        board.addUser(user);

        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));

        Set<User> result = boardService.getUsersFromBoard(1L);

        assertThat(result).contains(user);
    }
}
