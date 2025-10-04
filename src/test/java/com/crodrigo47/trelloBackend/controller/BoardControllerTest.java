package com.crodrigo47.trelloBackend.controller;

import com.crodrigo47.trelloBackend.config.JwtAuthenticationFilter;
import com.crodrigo47.trelloBackend.helper.Builders;
import com.crodrigo47.trelloBackend.model.Board;
import com.crodrigo47.trelloBackend.model.Task;
import com.crodrigo47.trelloBackend.model.User;
import com.crodrigo47.trelloBackend.service.BoardService;
import com.crodrigo47.trelloBackend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BoardController.class)
@AutoConfigureMockMvc(addFilters = false)
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private BoardService boardService;

    @MockBean
    private UserService userService; // mock para resolver principal -> User

    private User mockUser;

    @BeforeEach
    void setup() {
        mockUser = Builders.buildUserWithId("alice", 1L);
        Mockito.when(userService.getUserByUsername(Mockito.anyString()))
               .thenReturn(Optional.of(mockUser));
    }

    @Test
    void testGetAllBoards() throws Exception {
        Board board1 = Builders.buildBoardWithId("Board 1", 1L, mockUser);
        Board board2 = Builders.buildBoardWithId("Board 2", 2L, mockUser);

        Mockito.when(boardService.getAllBoardsForCurrentUser(Mockito.any()))
               .thenReturn(List.of(board1, board2));

        mockMvc.perform(get("/boards").principal(() -> "alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Board 1"))
                .andExpect(jsonPath("$[1].name").value("Board 2"));
    }

    @Test
    void testGetBoardById() throws Exception {
        Board board = Builders.buildBoardWithId("Board 1", 1L, mockUser);

        Mockito.when(boardService.getBoardById(Mockito.eq(1L), Mockito.any()))
               .thenReturn(board);

        mockMvc.perform(get("/boards/1").principal(() -> "alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Board 1"))
                .andExpect(jsonPath("$.description").value("Descripción de Board 1"));
    }

    @Test
    void testCreateBoard() throws Exception {
        Board board = Builders.buildBoardWithId("New Board", 3L, mockUser);

        Mockito.when(boardService.createBoard(Mockito.any(Board.class), Mockito.any()))
               .thenReturn(board);

        mockMvc.perform(post("/boards")
                        .principal(() -> "alice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New Board\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Board"));
    }

    @Test
    void testUpdateBoard() throws Exception {
        Board board = Builders.buildBoardWithId("Updated Board", 1L, mockUser);

        Mockito.when(boardService.updateBoard(Mockito.any(Board.class), Mockito.any()))
               .thenReturn(board);

        mockMvc.perform(put("/boards/1")
                        .principal(() -> "alice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Board\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Board"));
    }

    @Test
    void testDeleteBoard() throws Exception {
        Mockito.doNothing().when(boardService).deleteBoard(Mockito.eq(1L), Mockito.any());

        mockMvc.perform(delete("/boards/1").principal(() -> "alice"))
                .andExpect(status().isOk());
    }

    @Test
    void testAddTaskToBoard() throws Exception {
        Board board = Builders.buildBoardWithId("Board 1", 1L, mockUser);

        Mockito.when(boardService.addTaskToBoard(Mockito.eq(1L), Mockito.any(Task.class), Mockito.any()))
               .thenReturn(board);

        mockMvc.perform(post("/boards/1/tasks")
                        .principal(() -> "alice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"New Task\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Board 1"));
    }
}
