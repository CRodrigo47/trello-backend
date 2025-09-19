package com.crodrigo47.trelloBackend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.crodrigo47.trelloBackend.helper.Builders;
import com.crodrigo47.trelloBackend.model.Task;
import com.crodrigo47.trelloBackend.repository.TaskRepository;


@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    
    @Mock
    TaskRepository taskRepository;

    @InjectMocks
    TaskService taskService;

    @Test
    void getAllTasks_returnList(){
        when(taskRepository.findAll()).thenReturn(List.of(
            Builders.buildTask("tarea 1", Builders.buildBoard("Diseño"), Builders.buildUser("bob")),
            Builders.buildTask("tarea 2", Builders.buildBoard("Programación"), Builders.buildUser("alice"))));

        var result = taskService.getAllTasks();

        assertThat(result).hasSize(2);
        verify(taskRepository).findAll();
    }

    @Test
    void getTaskById_returnTask(){
        when(taskRepository.findById(2L)).thenReturn(Optional.of(
            Builders.buildTaskWithId("tarea 3", 3L, Builders.buildBoard("Comunicación"), Builders.buildUser("charlie"))));

        var result = taskService.getTaskById(2L);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("tarea 3");
        assertThat(result.get().getAssignedTo().getUsername()).isEqualTo("charlie");
        assertThat(result.get().getBoard().getName()).isEqualTo("Comunicación");
    }

    @Test
    void createTask_returnTask(){
        Task taskToSave = Builders.buildTask("tarea 1", Builders.buildBoard("Diseño"), Builders.buildUser("bob"));

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task arg = invocation.getArgument(0);
            arg.setId(1L); // simulo id generado por la BD
            arg.getBoard().setId(5L); // simulo board existente en BD
            arg.getAssignedTo().setId(2L); // simulo user existente en BD
            arg.setCreatedAt(LocalDateTime.now()); // simulo fecha de creación
        return arg;
        });

        Task result = taskService.createTask(taskToSave);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAssignedTo().getId()).isEqualTo(2L);
        assertThat(result.getBoard().getId()).isEqualTo(5L);
        assertThat(result.getTitle()).isEqualTo("tarea 1");
        assertThat(result.getCreatedAt()).isNotNull();

    }

    @Test
    void updateTask_returnTask(){
        Task taskToSave = Builders.buildTaskWithId("tarea 2", 1L, Builders.buildBoardWithId("Diseño", 5L), Builders.buildUserWithId("bob", 10L));

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task arg = invocation.getArgument(0);
            arg.setId(taskToSave.getId());
            arg.setBoard(taskToSave.getBoard());
            arg.setAssignedTo(taskToSave.getAssignedTo());
            arg.setUpdatedAt(taskToSave.getUpdatedAt());
        return arg;
        });

        Task result = taskService.updateTask(taskToSave);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAssignedTo().getId()).isEqualTo(10L);
        assertThat(result.getBoard().getId()).isEqualTo(5L);
        assertThat(result.getTitle()).isEqualTo("tarea 2");
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    void deleteTask_callsRepository(){
        taskService.deleteTask(10L);

        verify(taskRepository).deleteById(10L);
    }
}
