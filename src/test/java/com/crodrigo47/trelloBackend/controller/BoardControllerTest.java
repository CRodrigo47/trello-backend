package com.crodrigo47.trelloBackend.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.crodrigo47.trelloBackend.service.TaskService;
import com.crodrigo47.trelloBackend.helper.Builders;
import com.crodrigo47.trelloBackend.service.BoardService;
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
        when(boardService.getAllBoards()).thenReturn(List.of(Builders.buildBoardWithId("MockBoard", 1L)));

        mockMvc.perform(get("/boards"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("MockBoard"));
    }
    
}
