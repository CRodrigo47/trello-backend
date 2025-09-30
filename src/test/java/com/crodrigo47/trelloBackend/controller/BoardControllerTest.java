package com.crodrigo47.trelloBackend.controller;

import com.crodrigo47.trelloBackend.config.JwtAuthenticationFilter;
import com.crodrigo47.trelloBackend.helper.Builders;
import com.crodrigo47.trelloBackend.model.Board;
import com.crodrigo47.trelloBackend.model.Task;
import com.crodrigo47.trelloBackend.model.User;
import com.crodrigo47.trelloBackend.service.BoardService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BoardController.class)
@AutoConfigureMockMvc(addFilters = false) // desactiva seguridad para los tests
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @MockBean
    private BoardService boardService;

    // ------------------- GET ALL BOARDS -------------------
    @Test
    void testGetAllBoards() throws Exception {
        User user = Builders.buildUserWithId("alice", 1L);
        Board board1 = Builders.buildBoardWithId("Board 1", 1L, user);
        Board board2 = Builders.buildBoardWithId("Board 2", 2L, user);

        Mockito.when(boardService.getAllBoardsForCurrentUser(Mockito.any()))
               .thenReturn(List.of(board1, board2));

        mockMvc.perform(get("/boards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Board 1"))
                .andExpect(jsonPath("$[1].name").value("Board 2"));
    }

    // ------------------- GET BOARD BY ID -------------------
    @Test
    void testGetBoardById() throws Exception {
        User user = Builders.buildUserWithId("alice", 1L);
        Board board = Builders.buildBoardWithId("Board 1", 1L, user);

        Mockito.when(boardService.getBoardById(Mockito.eq(1L), Mockito.any()))
               .thenReturn(board);

        mockMvc.perform(get("/boards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Board 1"))
                .andExpect(jsonPath("$.description").value("Descripción de Board 1"));
    }

    // ------------------- CREATE BOARD -------------------
    @Test
    void testCreateBoard() throws Exception {
        User user = Builders.buildUserWithId("alice", 1L);
        Board board = Builders.buildBoardWithId("New Board", 3L, user);

        Mockito.when(boardService.createBoard(Mockito.any(Board.class), Mockito.any()))
               .thenReturn(board);

        mockMvc.perform(post("/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New Board\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Board"));
    }

    // ------------------- UPDATE BOARD -------------------
    @Test
    void testUpdateBoard() throws Exception {
        User user = Builders.buildUserWithId("alice", 1L);
        Board board = Builders.buildBoardWithId("Updated Board", 1L, user);

        Mockito.when(boardService.updateBoard(Mockito.any(Board.class), Mockito.any()))
               .thenReturn(board);

        mockMvc.perform(put("/boards/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Board\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Board"));
    }

    // ------------------- DELETE BOARD -------------------
    @Test
    void testDeleteBoard() throws Exception {
        Mockito.doNothing().when(boardService).deleteBoard(Mockito.eq(1L), Mockito.any());

        mockMvc.perform(delete("/boards/1"))
                .andExpect(status().isOk());
    }

    // ------------------- ADD TASK TO BOARD -------------------
    @Test
    void testAddTaskToBoard() throws Exception {
        User user = Builders.buildUserWithId("alice", 1L);
        Board board = Builders.buildBoardWithId("Board 1", 1L, user);

        Mockito.when(boardService.addTaskToBoard(Mockito.eq(1L), Mockito.any(Task.class), Mockito.any()))
               .thenReturn(board);

        mockMvc.perform(post("/boards/1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"New Task\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Board 1"));
    }
}
