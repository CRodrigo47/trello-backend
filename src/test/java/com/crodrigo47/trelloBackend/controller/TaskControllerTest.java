package com.crodrigo47.trelloBackend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.web.servlet.MockMvc;

import com.crodrigo47.trelloBackend.config.JwtAuthenticationFilter;
import com.crodrigo47.trelloBackend.exception.TaskNotFoundException;
import com.crodrigo47.trelloBackend.helper.Builders;
import com.crodrigo47.trelloBackend.model.Board;
import com.crodrigo47.trelloBackend.model.Task;
import com.crodrigo47.trelloBackend.model.User;
import com.crodrigo47.trelloBackend.service.TaskService;
import com.crodrigo47.trelloBackend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;

    @MockBean JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean TaskService taskService;
    @MockBean UserService userService;

    @Test
    void getTaskById_returnsTask() throws Exception {
        User currentUser = Builders.buildUserWithId("alice", 5L);
        Board board = Builders.buildBoard("BoardTest", currentUser);
        Task task = Builders.buildTaskWithId("MockTask", 1L, board, currentUser, currentUser);

        when(taskService.getTaskById(eq(1L), any())).thenReturn(task);

        mockMvc.perform(get("/tasks/1")
                .with(authentication(new UsernamePasswordAuthenticationToken(currentUser, null))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("MockTask"))
            .andExpect(jsonPath("$.description").value("Descripción de MockTask"));
    }

    @Test
    void createTask_returnsCreatedTask() throws Exception {
        User currentUser = Builders.buildUserWithId("alice", 5L);
        Board board = Builders.buildBoard("BoardTest", currentUser);
        Task input = Builders.buildTask("NewTask", board, currentUser, currentUser);
        Task saved = Builders.buildTaskWithId("NewTask", 1L, board, currentUser, currentUser);

        when(taskService.createTask(any(Task.class), any())).thenReturn(saved);

        mockMvc.perform(post("/tasks")
            .with(authentication(new UsernamePasswordAuthenticationToken(currentUser, null)))
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(input)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("NewTask"));
    }

    @Test
    void updateTask_returnsUpdatedTask() throws Exception {
        User currentUser = Builders.buildUserWithId("alice", 5L);
        Board board = Builders.buildBoard("BoardTest", currentUser);
        Task updated = Builders.buildTaskWithId("UpdatedTask", 1L, board, currentUser, currentUser);

        when(taskService.updateTask(any(Task.class), any())).thenReturn(updated);

        mockMvc.perform(put("/tasks/1")
            .with(authentication(new UsernamePasswordAuthenticationToken(currentUser, null)))
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(updated)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("UpdatedTask"));
    }

    @Test
    void deleteTask_callsService() throws Exception {
        User currentUser = Builders.buildUserWithId("alice", 5L);
        Long taskId = 1L;

        mockMvc.perform(delete("/tasks/" + taskId)
                .with(authentication(new UsernamePasswordAuthenticationToken(currentUser, null))))
            .andExpect(status().isOk());

        verify(taskService).deleteTask(eq(taskId), any());
    }

    @Test
    void assignUser_returnsTask() throws Exception {
        User currentUser = Builders.buildUserWithId("manager", 99L);
        User assignee = Builders.buildUserWithId("bob", 10L);
        Board board = Builders.buildBoard("BoardTest", currentUser);
        Task task = Builders.buildTaskWithId("TaskWithUser", 1L, board, assignee, assignee);

        when(userService.getUserById(10L)).thenReturn(Optional.of(assignee));
        when(taskService.assignTaskToUser(eq(1L), any(), eq(assignee))).thenReturn(task);

        mockMvc.perform(post("/tasks/1/users/10")
                .with(authentication(new UsernamePasswordAuthenticationToken(currentUser, null))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("TaskWithUser"))
            .andExpect(jsonPath("$.description").value("Descripción de TaskWithUser"));
    }

    @Test
    void unassignUser_returnsTask() throws Exception {
        User currentUser = Builders.buildUserWithId("manager", 99L);
        Board board = Builders.buildBoard("BoardTest", currentUser);
        Task task = Builders.buildTaskWithId("TaskWithoutUser", 1L, board, currentUser, currentUser);

        when(taskService.unassignTaskFromUser(eq(1L), any())).thenReturn(task);

        mockMvc.perform(delete("/tasks/1/users")
                .with(authentication(new UsernamePasswordAuthenticationToken(currentUser, null))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("TaskWithoutUser"));
    }

    @Test
    void getUserAssigned_returnsUser() throws Exception {
        User currentUser = Builders.buildUserWithId("manager", 99L);
        User assigned = Builders.buildUserWithId("alice", 5L);
        Board board = Builders.buildBoard("BoardTest", currentUser);
        Task task = Builders.buildTaskWithId("Task", 1L, board, assigned, assigned);

        when(taskService.getTaskById(eq(1L), any())).thenReturn(task);

        mockMvc.perform(get("/tasks/1/users")
                .with(authentication(new UsernamePasswordAuthenticationToken(currentUser, null))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(5))
            .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void getTasksByBoard_returnsTaskList() throws Exception {
        User currentUser = Builders.buildUserWithId("alice", 5L);
        Board board = Builders.buildBoard("BoardTest", currentUser);
        Task task = Builders.buildTaskWithId("BoardTask", 1L, board, currentUser, currentUser);
    
        when(taskService.getTasksByBoard(eq(1L), eq(currentUser.getId()))).thenReturn(List.of(task));
    
        Authentication auth = new UsernamePasswordAuthenticationToken(currentUser, null);
        SecurityContextHolder.setContext(new SecurityContextImpl(auth)); // <-- aquí ponemos la auth en el contexto
        try {
            mockMvc.perform(get("/tasks/board/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("BoardTask"));
        } finally {
            SecurityContextHolder.clearContext(); // limpiamos siempre
        }
    }
    
    @Test
    void getTasksByUser_returnsTaskList() throws Exception {
        User currentUser = Builders.buildUserWithId("alice", 5L);
        Board board = Builders.buildBoard("BoardTest", currentUser);
        Task task = Builders.buildTaskWithId("UserTask", 1L, board, currentUser, currentUser);
    
        when(taskService.getTasksByUser(eq(2L), eq(currentUser.getId()))).thenReturn(List.of(task));
    
        Authentication auth = new UsernamePasswordAuthenticationToken(currentUser, null);
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));
        try {
            mockMvc.perform(get("/tasks/user/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("UserTask"));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
    
    
    @Test
    void getTasksByStatus_returnsTaskList() throws Exception {
        User currentUser = Builders.buildUserWithId("alice", 5L);
        Board board = Builders.buildBoard("BoardTest", currentUser);
        Task task = Builders.buildTaskWithId("StatusTask", 1L, board, currentUser, currentUser);
        task.setStatus(Task.Status.FUTURE);
    
        when(taskService.getTasksByStatus(eq(1L), eq(Task.Status.FUTURE), eq(currentUser.getId()))).thenReturn(List.of(task));
    
        Authentication auth = new UsernamePasswordAuthenticationToken(currentUser, null);
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));
        try {
            mockMvc.perform(get("/tasks/status/FUTURE/board/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("StatusTask"));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }


    // ------------------- ERROR CASES -------------------

    @Test
    void assignUser_userNotFound_returns404() throws Exception {
        User currentUser = Builders.buildUserWithId("manager", 99L);
        when(userService.getUserById(10L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/tasks/1/users/10")
                .with(authentication(new UsernamePasswordAuthenticationToken(currentUser, null))))
            .andExpect(status().isNotFound());
    }

    @Test
    void getUserAssigned_taskNotFound_returns404() throws Exception {
        User currentUser = Builders.buildUserWithId("manager", 99L);
        when(taskService.getTaskById(eq(1L), any())).thenThrow(new TaskNotFoundException("not found"));

        mockMvc.perform(get("/tasks/1/users")
                .with(authentication(new UsernamePasswordAuthenticationToken(currentUser, null))))
            .andExpect(status().isNotFound());
    }
}
