package com.crodrigo47.trelloBackend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.crodrigo47.trelloBackend.helper.Builders;
import com.crodrigo47.trelloBackend.helper.BuildersDto;
import com.crodrigo47.trelloBackend.model.Task;
import com.crodrigo47.trelloBackend.model.User;
import com.crodrigo47.trelloBackend.service.TaskService;
import com.crodrigo47.trelloBackend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;

    @MockBean TaskService taskService;
    @MockBean UserService userService;

    @Test
    void getAllTasks_returnsJsonList() throws Exception {
        Task task = Builders.buildTaskWithId("MockTask", 1L, null, null);
        when(taskService.getAllTasks()).thenReturn(List.of(task));

        var expectedDto = BuildersDto.buildTaskDtoWithId("MockTask", 1L);

        mockMvc.perform(get("/tasks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(expectedDto.id()))
            .andExpect(jsonPath("$[0].title").value(expectedDto.title()));
    }

    @Test
    void getTaskById_returnsTask() throws Exception {
        Task task = Builders.buildTaskWithId("MockTask", 1L, null, null);
        when(taskService.getTaskById(1L)).thenReturn(Optional.of(task));

        var expectedDto = BuildersDto.buildTaskDtoWithId("MockTask", 1L);

        mockMvc.perform(get("/tasks/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(expectedDto.id()))
            .andExpect(jsonPath("$.title").value(expectedDto.title()));
    }

    @Test
    void createTask_returnsCreatedTask() throws Exception {
        Task input = Builders.buildTask("NewTask", null, null);
        Task saved = Builders.buildTaskWithId("NewTask", 1L, null, null);
        when(taskService.createTask(any(Task.class))).thenReturn(saved);

        var expectedDto = BuildersDto.buildTaskDtoWithId("NewTask", 1L);

        mockMvc.perform(post("/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(input)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(expectedDto.id()))
            .andExpect(jsonPath("$.title").value(expectedDto.title()));
    }

    @Test
    void updateTask_returnsUpdatedTask() throws Exception {
        Task updated = Builders.buildTaskWithId("UpdatedTask", 1L, null, null);
        when(taskService.updateTask(any(Task.class))).thenReturn(updated);

        var expectedDto = BuildersDto.buildTaskDtoWithId("UpdatedTask", 1L);

        mockMvc.perform(put("/tasks/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(updated)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(expectedDto.id()))
            .andExpect(jsonPath("$.title").value(expectedDto.title()));
    }

    @Test
    void deleteTask_callsService() throws Exception {
        Long taskId = 1L;

        mockMvc.perform(delete("/tasks/" + taskId))
            .andExpect(status().isOk());

        verify(taskService).deleteTask(taskId);
    }

    @Test
    void assignUser_returnsTask() throws Exception {
        User user = Builders.buildUserWithId("bob", 10L);
        Task task = Builders.buildTaskWithId("TaskWithUser", 1L, null, user);

        when(userService.getUserById(10L)).thenReturn(Optional.of(user));
        when(taskService.assignTaskToUser(1L, user)).thenReturn(task);

        var expectedDto = BuildersDto.buildTaskDtoWithId("TaskWithUser", 1L);

        mockMvc.perform(post("/tasks/1/users/10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(expectedDto.id()))
            .andExpect(jsonPath("$.title").value(expectedDto.title()));
    }

    @Test
    void unassignUser_returnsTask() throws Exception {
        Task task = Builders.buildTaskWithId("TaskWithoutUser", 1L, null, null);
        when(taskService.unassignTaskFromUser(1L)).thenReturn(task);

        var expectedDto = BuildersDto.buildTaskDtoWithId("TaskWithoutUser", 1L);

        mockMvc.perform(delete("/tasks/1/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(expectedDto.id()))
            .andExpect(jsonPath("$.title").value(expectedDto.title()));
    }

    @Test
    void getUserAssigned_returnsUser() throws Exception {
        User user = Builders.buildUserWithId("alice", 5L);
        Task task = Builders.buildTaskWithId("Task", 1L, null, user);

        when(taskService.getTaskById(1L)).thenReturn(Optional.of(task));

        var expectedDto = BuildersDto.buildUserDtoWithId("alice", 5L);

        mockMvc.perform(get("/tasks/1/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(expectedDto.id()))
            .andExpect(jsonPath("$.username").value(expectedDto.username()));
    }

    @Test
    void getTasksByBoard_returnsTaskList() throws Exception {
        Task task = Builders.buildTaskWithId("BoardTask", 1L, null, null);
        when(taskService.getTasksByBoard(1L)).thenReturn(List.of(task));

        var expectedDto = BuildersDto.buildTaskDtoWithId("BoardTask", 1L);

        mockMvc.perform(get("/tasks/board/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(expectedDto.id()))
            .andExpect(jsonPath("$[0].title").value(expectedDto.title()));
    }

    @Test
    void getTasksByUser_returnsTaskList() throws Exception {
        Task task = Builders.buildTaskWithId("UserTask", 1L, null, null);
        when(taskService.getTasksByUser(2L)).thenReturn(List.of(task));

        var expectedDto = BuildersDto.buildTaskDtoWithId("UserTask", 1L);

        mockMvc.perform(get("/tasks/user/2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(expectedDto.id()))
            .andExpect(jsonPath("$[0].title").value(expectedDto.title()));
    }

    @Test
    void getTasksByStatus_returnsTaskList() throws Exception {
        Task task = Builders.buildTaskWithId("StatusTask", 1L, null, null);
        task.setStatus(Task.Status.FUTURE);

        when(taskService.getTasksByStatus(Task.Status.FUTURE)).thenReturn(List.of(task));

        var expectedDto = BuildersDto.buildTaskDtoWithId("StatusTask", 1L);

        mockMvc.perform(get("/tasks/status/FUTURE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(expectedDto.id()))
            .andExpect(jsonPath("$[0].title").value(expectedDto.title()));
    }

    //-------------------------------ERROR TEST----------------------------------------//

    @Test
    void getTaskById_notFound_returns404() throws Exception {
        when(taskService.getTaskById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/tasks/1"))
            .andExpect(status().isNotFound());
    }

    @Test
    void assignUser_userNotFound_returns404() throws Exception {
        when(userService.getUserById(10L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/tasks/1/users/10"))
            .andExpect(status().isNotFound());
    }

    @Test
    void getUserAssigned_taskNotFound_returns404() throws Exception {
        when(taskService.getTaskById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/tasks/1/users"))
            .andExpect(status().isNotFound());
    }

}
