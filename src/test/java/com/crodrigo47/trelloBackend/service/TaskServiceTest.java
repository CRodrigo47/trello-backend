package com.crodrigo47.trelloBackend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.crodrigo47.trelloBackend.exception.TaskNotFoundException;
import com.crodrigo47.trelloBackend.helper.Builders;
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
    void getAllTasks_returnList() {
        when(taskRepository.findAll()).thenReturn(List.of(
                Builders.buildTask("tarea 1", Builders.buildBoard("Diseño", Builders.buildUser("bob")), Builders.buildUser("bob")),
                Builders.buildTask("tarea 2", Builders.buildBoard("Programación", Builders.buildUser("bob")), Builders.buildUser("alice"))));

        var result = taskService.getAllTasks();

        assertThat(result).hasSize(2);
        verify(taskRepository).findAll();
    }

    @Test
    void getTaskById_returnTask() {
        when(taskRepository.findById(2L)).thenReturn(Optional.of(
                Builders.buildTaskWithId("tarea 3", 3L, Builders.buildBoard("Comunicación", Builders.buildUser("bob")), Builders.buildUser("charlie"))));

        var result = taskService.getTaskById(2L);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("tarea 3");
    }

    @Test
    void createTask_returnTask() {
        Task taskToSave = Builders.buildTask("tarea 1", Builders.buildBoard("Diseño", Builders.buildUser("bob")), Builders.buildUser("bob"));

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task arg = invocation.getArgument(0);
            arg.setId(1L);
            arg.getBoard().setId(5L);
            arg.getAssignedTo().setId(2L);
            arg.setCreatedAt(LocalDateTime.now());
            return arg;
        });

        Task result = taskService.createTask(taskToSave);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    void updateTask_returnTask() {
        Task taskToSave = Builders.buildTaskWithId("tarea 2", 1L, Builders.buildBoardWithId("Diseño", 5L, Builders.buildUser("bob")),
                Builders.buildUserWithId("bob", 10L));

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task arg = invocation.getArgument(0);
            arg.setId(taskToSave.getId());
            arg.setBoard(taskToSave.getBoard());
            arg.setAssignedTo(taskToSave.getAssignedTo());
            arg.setUpdatedAt(LocalDateTime.now());
            return arg;
        });

        Task result = taskService.updateTask(taskToSave);

        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    void assignTaskToUser_returnTask() {
        Task task = Builders.buildTaskWithId("tarea", 1L, Builders.buildBoard("Diseño", Builders.buildUser("bob")), Builders.buildUser("bob"));
        User user = Builders.buildUserWithId("alice", 2L);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        var result = taskService.assignTaskToUser(1L, user);

        assertThat(result.getAssignedTo()).isEqualTo(user);
    }

    @Test
    void assignTaskToUser_taskNotFound_throwException() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.assignTaskToUser(1L, Builders.buildUser("bob")))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void unassignTaskFromUser_returnTask() {
        Task task = Builders.buildTaskWithId("tarea", 1L, Builders.buildBoard("Diseño", Builders.buildUser("bob")), Builders.buildUser("bob"));

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        var result = taskService.unassignTaskFromUser(1L);

        assertThat(result.getAssignedTo()).isNull();
    }

    @Test
    void getTasksByBoard_returnList() {
        when(taskRepository.findByBoardId(1L)).thenReturn(List.of(Builders.buildTask("tarea", Builders.buildBoard("Diseño", Builders.buildUser("bob")), Builders.buildUser("bob"))));

        var result = taskService.getTasksByBoard(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void deleteTask_callsRepository() {
        taskService.deleteTask(10L);

        verify(taskRepository).deleteById(10L);
    }
}
