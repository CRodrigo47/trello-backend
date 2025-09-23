package com.crodrigo47.trelloBackend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.crodrigo47.trelloBackend.helper.Builders;
import com.crodrigo47.trelloBackend.helper.BuildersDto;
import com.crodrigo47.trelloBackend.model.Board;
import com.crodrigo47.trelloBackend.model.Task;
import com.crodrigo47.trelloBackend.model.User;
import com.crodrigo47.trelloBackend.service.BoardService;
import com.crodrigo47.trelloBackend.service.TaskService;
import com.crodrigo47.trelloBackend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(BoardController.class)
@AutoConfigureMockMvc(addFilters = false)
class BoardControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;

    @MockBean BoardService boardService;
    @MockBean UserService userService;
    @MockBean TaskService taskService;

    @Test
    void getAllBoards_returnsJsonList() throws Exception {
        Board board = Builders.buildBoardWithId("MockBoard", 1L);
        when(boardService.getAllBoards()).thenReturn(List.of(board));

        var expectedDto = BuildersDto.buildBoardDtoWithId("MockBoard", 1L);

        mockMvc.perform(get("/boards"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(expectedDto.id()))
            .andExpect(jsonPath("$[0].name").value(expectedDto.name()));
    }

    @Test
    void getBoardById_returnsBoard() throws Exception {
        Board board = Builders.buildBoardWithId("MockBoard", 1L);
        when(boardService.getBoardById(1L)).thenReturn(Optional.of(board));

        var expectedDto = BuildersDto.buildBoardDtoWithId("MockBoard", 1L);

        mockMvc.perform(get("/boards/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(expectedDto.id()))
            .andExpect(jsonPath("$.name").value(expectedDto.name()));
    }

    @Test
    void createBoard_returnsCreatedBoard() throws Exception {
        Board inputBoard = Builders.buildBoard("TestBoard");
        Board saved = Builders.buildBoardWithId("TestBoard", 1L);
        when(boardService.createBoard(any(Board.class))).thenReturn(saved);

        var expectedDto = BuildersDto.buildBoardDtoWithId("TestBoard", 1L);

        mockMvc.perform(post("/boards")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(inputBoard)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(expectedDto.id()))
            .andExpect(jsonPath("$.name").value(expectedDto.name()));
    }

    @Test
    void updateBoard_returnsUpdatedBoard() throws Exception {
        Board updated = Builders.buildBoardWithId("TestBoard", 1L);
        when(boardService.updateBoard(any(Board.class))).thenReturn(updated);

        var expectedDto = BuildersDto.buildBoardDtoWithId("TestBoard", 1L);

        mockMvc.perform(put("/boards/" + updated.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(updated)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(expectedDto.id()))
            .andExpect(jsonPath("$.name").value(expectedDto.name()));
    }

    @Test
    void deleteBoard_callsService() throws Exception {
        Long boardId = 1L;

        mockMvc.perform(delete("/boards/" + boardId))
            .andExpect(status().isOk());

        verify(boardService).deleteBoard(boardId);
    }

    @Test
    void addUserToBoard_returnsBoard() throws Exception {
        Board saved = Builders.buildBoardWithId("TestBoard", 1L);
        User user = Builders.buildUserWithId("bob", 10L);

        saved.addUser(user);

        when(boardService.addUserToBoard(anyLong(), anyLong())).thenReturn(saved);

        var expectedDto = BuildersDto.buildBoardDtoWithId("TestBoard", 1L);
        expectedDto.userIds().add(user.getId());

        mockMvc.perform(post("/boards/" + saved.getId() + "/users/" + user.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(saved)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(expectedDto.id()))
            .andExpect(jsonPath("$.name").value(expectedDto.name()))
            .andExpect(jsonPath("$.userIds[0]").value(user.getId()));
    }

    @Test
    void removeUserFromBoard_callsService() throws Exception {
        Long boardId = 1L;
        Long userId = 10L;

        mockMvc.perform(delete("/boards/" + boardId + "/users/" + userId))
            .andExpect(status().isOk());

        verify(boardService).removeUserFromBoard(boardId, userId);
    }

    @Test
    void addTaskToBoard_returnsBoard() throws Exception {
        Board saved = Builders.buildBoardWithId("TestBoard", 1L);
        User user = Builders.buildUserWithId("bob", 10L);
        Task task = Builders.buildTaskWithId("TestTask", 100L, saved, user);

        saved.addTask(task);

        when(boardService.addTaskToBoard(anyLong(), any(Task.class))).thenReturn(saved);

        var expectedDto = BuildersDto.buildBoardDtoWithId("TestBoard", 1L);
        expectedDto.taskIds().add(task.getId());

        mockMvc.perform(post("/boards/" + saved.getId() + "/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(saved)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(expectedDto.id()))
            .andExpect(jsonPath("$.name").value(expectedDto.name()))
            .andExpect(jsonPath("$.taskIds[0]").value(task.getId()));
    }

    @Test
    void removeTaskFromBoard_callsService() throws Exception {
        Long boardId = 1L;
        Long taskId = 10L;

        mockMvc.perform(delete("/boards/" + boardId + "/tasks/" + taskId))
            .andExpect(status().isOk());

        verify(boardService).removeTaskFromBoard(boardId, taskId);
    }

    @Test
    void getTaskFromBoard_returnsTaskList() throws Exception {
        Board board = Builders.buildBoardWithId("TestBoard", 1L);
        User user = Builders.buildUserWithId("bob", 10L);

        Task task1 = Builders.buildTaskWithId("Task 1", 100L, board, user);
        Task task2 = Builders.buildTaskWithId("Task 2", 101L, board, user);

        var expectedTask1 = BuildersDto.buildTaskDtoWithId("Task 1", 100L);
        var expectedTask2 = BuildersDto.buildTaskDtoWithId("Task 2", 101L);

        when(boardService.getTasksFromBoard(board.getId())).thenReturn(Set.of(task1, task2));

        mockMvc.perform(get("/boards/" + board.getId() + "/tasks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(expectedTask1.id()))
            .andExpect(jsonPath("$[0].title").value(expectedTask1.title()))
            .andExpect(jsonPath("$[1].id").value(expectedTask2.id()))
            .andExpect(jsonPath("$[1].title").value(expectedTask2.title()));
    }
    
    @Test
    void getUserFromBoard_returnsUserList() throws Exception {
        Long boardId = 1L;

        User user1 = Builders.buildUserWithId("alice", 10L);
        User user2 = Builders.buildUserWithId("bob", 11L);

        var expectedUser1 = BuildersDto.buildUserDtoWithId("alice", 10L);
        var expectedUser2 = BuildersDto.buildUserDtoWithId("bob", 11L);

        when(boardService.getUsersFromBoard(boardId)).thenReturn(Set.of(user1, user2));

        mockMvc.perform(get("/boards/" + boardId + "/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(expectedUser1.id()))
            .andExpect(jsonPath("$[0].username").value(expectedUser1.username()))
            .andExpect(jsonPath("$[1].id").value(expectedUser2.id()))
            .andExpect(jsonPath("$[1].username").value(expectedUser2.username()));
    }
        
}
