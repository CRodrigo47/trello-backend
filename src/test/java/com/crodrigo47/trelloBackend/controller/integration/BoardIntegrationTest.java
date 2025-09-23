package com.crodrigo47.trelloBackend.controller.integration;

import com.crodrigo47.trelloBackend.helper.Builders;
import com.crodrigo47.trelloBackend.model.Board;
import com.crodrigo47.trelloBackend.model.Task;
import com.crodrigo47.trelloBackend.model.User;
import com.crodrigo47.trelloBackend.repository.BoardRepository;
import com.crodrigo47.trelloBackend.repository.TaskRepository;
import com.crodrigo47.trelloBackend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
    "jwt.secret=${JWT_TEST_SECRET:supersecuretestkeythatisatleast32bytes!}",
    "jwt.expiration-ms=3600000"
})
class BoardIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;
    @Autowired BoardRepository boardRepository;
    @Autowired UserRepository userRepository;
    @Autowired TaskRepository taskRepository;

    @BeforeEach
    void setup() {
        taskRepository.deleteAll();
        boardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void boardFullFlow_crudAndRelations() throws Exception {
        // ----------------- CREATE -----------------
        String boardName = "IntegrationBoard";
        String updatedBoardName = "UpdatedBoard";

        String createResponse = mockMvc.perform(post("/boards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("name", boardName))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> boardJson = mapper.readValue(createResponse, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        Long boardId = Long.valueOf((Integer) boardJson.get("id"));
        assertThat(boardJson.get("name")).isEqualTo(boardName);
        assertThat(boardRepository.findById(boardId)).isPresent();

        // ----------------- UPDATE -----------------
        boardJson.put("name", updatedBoardName);
        String updateResponse = mockMvc.perform(put("/boards/" + boardId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(boardJson)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> updatedBoardJson = mapper.readValue(updateResponse, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        assertThat(updatedBoardJson.get("name")).isEqualTo(updatedBoardName);

        // ----------------- ADD USER -----------------
        User boardUser = userRepository.save(Builders.buildUser("alice"));
        String addUserResponse = mockMvc.perform(post("/boards/" + boardId + "/users/" + boardUser.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> boardWithUserJson = mapper.readValue(addUserResponse, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        @SuppressWarnings("unchecked")
        List<Object> userIds = (List<Object>) boardWithUserJson.get("userIds");
        assertThat(userIds).contains(boardUser.getId().intValue());

        // ----------------- ADD TASK -----------------
        Board boardEntity = boardRepository.findById(boardId).orElseThrow();
        Task boardTask = taskRepository.save(Builders.buildTask("task1", boardEntity, boardUser));
        String addTaskResponse = mockMvc.perform(post("/boards/" + boardId + "/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(Map.of("id", boardTask.getId()))))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        Map<String, Object> boardWithTaskJson = mapper.readValue(addTaskResponse, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        @SuppressWarnings("unchecked")
        List<Object> taskIds = (List<Object>) boardWithTaskJson.get("taskIds");
        assertThat(taskIds).contains(boardTask.getId().intValue());

        // ----------------- GET TASKS -----------------
        mockMvc.perform(get("/boards/" + boardId + "/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(boardTask.getId()))
                .andExpect(jsonPath("$[0].title").value(boardTask.getTitle()));

        // ----------------- GET USERS -----------------
        mockMvc.perform(get("/boards/" + boardId + "/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(boardUser.getId()))
                .andExpect(jsonPath("$[0].username").value(boardUser.getUsername()));

        // ----------------- REMOVE USER -----------------
        mockMvc.perform(delete("/boards/" + boardId + "/users/" + boardUser.getId()))
                .andExpect(status().isOk());

        // ----------------- REMOVE TASK -----------------
        mockMvc.perform(delete("/boards/" + boardId + "/tasks/" + boardTask.getId()))
                .andExpect(status().isOk());

        // ----------------- DELETE BOARD -----------------
        mockMvc.perform(delete("/boards/" + boardId))
                .andExpect(status().isOk());
        assertThat(boardRepository.findById(boardId)).isEmpty();
    }
}
