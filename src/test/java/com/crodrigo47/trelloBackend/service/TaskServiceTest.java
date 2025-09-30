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

import com.crodrigo47.trelloBackend.exception.TaskNotFoundException;
import com.crodrigo47.trelloBackend.helper.Builders;
import com.crodrigo47.trelloBackend.model.Board;
import com.crodrigo47.trelloBackend.model.Task;
import com.crodrigo47.trelloBackend.model.User;
import com.crodrigo47.trelloBackend.repository.TaskRepository;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    TaskRepository taskRepository;

    @InjectMocks
    TaskService taskService;

    @Test
    void getTaskById_authorizedUser_returnsTask() {
        User creator = Builders.buildUserWithId("bob", 1L);
        Board board = Builders.buildBoardWithId("Diseño", 2L, creator);
        board.addUser(creator);

        Task task = Builders.buildTaskWithId("tarea", 3L, board, creator, creator);
        when(taskRepository.findById(3L)).thenReturn(Optional.of(task));

        var result = taskService.getTaskById(3L, creator);

        assertThat(result).isEqualTo(task);
    }

    @Test
    void getTaskById_notFound_throwsException() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(99L, Builders.buildUser("alice")))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void getTaskById_unauthorizedUser_throwsException() {
        User creator = Builders.buildUserWithId("bob", 1L);
        User outsider = Builders.buildUserWithId("mallory", 2L);
        Board board = Builders.buildBoardWithId("Diseño", 3L, creator);

        Task task = Builders.buildTaskWithId("tarea", 4L, board, creator, creator);
        when(taskRepository.findById(4L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.getTaskById(4L, outsider))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Not authorized");
    }

    @Test
    void createTask_userIsMember_savesTask() {
        User creator = Builders.buildUserWithId("bob", 1L);
        Board board = Builders.buildBoardWithId("Diseño", 2L, creator);
        board.addUser(creator);

        Task task = Builders.buildTask("nueva tarea", board, creator, creator);

        when(taskRepository.save(any(Task.class))).thenReturn(task);

        Task result = taskService.createTask(task, creator);

        assertThat(result.getCreatedBy()).isEqualTo(creator);
        verify(taskRepository).save(task);
    }

    @Test
    void createTask_userNotMember_throwsException() {
        User creator = Builders.buildUserWithId("bob", 1L);
        User outsider = Builders.buildUserWithId("mallory", 2L);
        Board board = Builders.buildBoardWithId("Diseño", 3L, creator);

        Task task = Builders.buildTask("nueva tarea", board, creator, creator);

        assertThatThrownBy(() -> taskService.createTask(task, outsider))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("must be a member");
    }

    @Test
    void updateTask_existingTask_updatesFields() {
        User creator = Builders.buildUserWithId("bob", 1L);
        Board board = Builders.buildBoardWithId("Diseño", 2L, creator);
        board.addUser(creator);

        Task existing = Builders.buildTaskWithId("vieja tarea", 3L, board, creator, creator);
        Task updated = Builders.buildTaskWithId("nueva tarea", 3L, board, creator, creator);

        when(taskRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(Task.class))).thenReturn(updated);

        Task result = taskService.updateTask(updated, creator);

        assertThat(result.getTitle()).isEqualTo("nueva tarea");
        verify(taskRepository).save(existing);
    }

    @Test
    void deleteTask_existingTask_deletesTask() {
        User creator = Builders.buildUserWithId("bob", 1L);
        Board board = Builders.buildBoardWithId("Diseño", 2L, creator);
        board.addUser(creator);

        Task task = Builders.buildTaskWithId("tarea", 3L, board, creator, creator);
        when(taskRepository.findById(3L)).thenReturn(Optional.of(task));

        taskService.deleteTask(3L, creator);

        verify(taskRepository).delete(task);
    }

    @Test
    void assignTaskToUser_validAssignee_assignsUser() {
        User creator = Builders.buildUserWithId("bob", 1L);
        User assignee = Builders.buildUserWithId("alice", 2L);

        Board board = Builders.buildBoardWithId("Diseño", 3L, creator);
        board.addUser(creator);
        board.addUser(assignee);

        Task task = Builders.buildTaskWithId("tarea", 4L, board, creator, creator);

        when(taskRepository.findById(4L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        Task result = taskService.assignTaskToUser(4L, creator, assignee);

        assertThat(result.getAssignedTo()).isEqualTo(assignee);
    }

    @Test
    void assignTaskToUser_userNotOnBoard_throwsException() {
        User creator = Builders.buildUserWithId("bob", 1L);
        User outsider = Builders.buildUserWithId("mallory", 2L);

        Board board = Builders.buildBoardWithId("Diseño", 3L, creator);
        board.addUser(creator);

        Task task = Builders.buildTaskWithId("tarea", 4L, board, creator, creator);

        when(taskRepository.findById(4L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.assignTaskToUser(4L, creator, outsider))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("must be a member");
    }

    @Test
    void unassignTaskFromUser_setsAssignedToNull() {
        User creator = Builders.buildUserWithId("bob", 1L);
        Board board = Builders.buildBoardWithId("Diseño", 2L, creator);
        board.addUser(creator);

        Task task = Builders.buildTaskWithId("tarea", 3L, board, creator, creator);
        task.assignUser(creator);

        when(taskRepository.findById(3L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        Task result = taskService.unassignTaskFromUser(3L, creator);

        assertThat(result.getAssignedTo()).isNull();
    }

    @Test
    void getTasksByBoard_returnsList() {
        when(taskRepository.findByBoardIdAndBoardUsersId(1L, 2L))
                .thenReturn(List.of(Builders.buildTask("tarea", Builders.buildBoard("Diseño", Builders.buildUser("bob")), Builders.buildUser("bob"), Builders.buildUser("bob"))));

        var result = taskService.getTasksByBoard(1L, 2L);

        assertThat(result).hasSize(1);
    }

    @Test
    void getTasksByUser_returnsList() {
        when(taskRepository.findByAssignedToIdAndBoardUsersId(2L, 3L))
                .thenReturn(List.of(Builders.buildTask("tarea", Builders.buildBoard("Programación", Builders.buildUser("alice")), Builders.buildUser("alice"), Builders.buildUser("alice"))));

        var result = taskService.getTasksByUser(2L, 3L);

        assertThat(result).hasSize(1);
    }

    @Test
    void getTasksByStatus_returnsList() {
        when(taskRepository.findByBoardIdAndStatusAndBoardUsersId(1L, Task.Status.DONE, 2L))
                .thenReturn(List.of(Builders.buildTaskWithStatus("tarea", Builders.buildBoard("Diseño", Builders.buildUser("bob")), Builders.buildUser("bob"), Builders.buildUser("bob"), Task.Status.DONE)));

        var result = taskService.getTasksByStatus(1L, Task.Status.DONE, 2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(Task.Status.DONE);
    }
}
