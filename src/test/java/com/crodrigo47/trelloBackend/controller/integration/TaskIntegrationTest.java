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
class TaskIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;
    @Autowired TaskRepository taskRepository;
    @Autowired BoardRepository boardRepository;
    @Autowired UserRepository userRepository;

    @BeforeEach
    void setup() {
        taskRepository.deleteAll();
        boardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void taskFullFlow_crudAndRelations() throws Exception {
        // ----------------- SETUP -----------------
        Board testBoard = boardRepository.save(Builders.buildBoard("BoardTest"));

        // CREATE USER via endpoint
        User testUser = Builders.buildUser("bob");

        String userResponse = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(testUser)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> userJson = mapper.readValue(userResponse, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        Long userId = Long.valueOf((Integer) userJson.get("id"));
        assertThat(userJson.get("username")).isEqualTo("bob");

        // ----------------- CREATE TASK -----------------
        Task testTask = Builders.buildTask("TestTask", testBoard, null);

        String createResponse = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(testTask)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> taskJson = mapper.readValue(createResponse, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        Long taskId = Long.valueOf((Integer) taskJson.get("id"));
        assertThat(taskJson.get("title")).isEqualTo("TestTask");

        // ----------------- UPDATE TASK -----------------
        taskJson.put("title", "UpdatedTask");
        String updateResponse = mockMvc.perform(put("/tasks/" + taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(taskJson)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> updatedTaskJson = mapper.readValue(updateResponse, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        assertThat(updatedTaskJson.get("title")).isEqualTo("UpdatedTask");

        // ----------------- GET TASK -----------------
        mockMvc.perform(get("/tasks/" + taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("UpdatedTask"));

        // ----------------- ASSIGN USER -----------------
        String assignResponse = mockMvc.perform(post("/tasks/" + taskId + "/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> assignedTaskJson = mapper.readValue(assignResponse, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        assertThat(((Integer) assignedTaskJson.get("assignedToId"))).isEqualTo(userId.intValue());

        // ----------------- GET USER ASSIGNED -----------------
        String getUserResp = mockMvc.perform(get("/tasks/" + taskId + "/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> userAssigned = mapper.readValue(getUserResp, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        assertThat(((Integer) userAssigned.get("id"))).isEqualTo(userId.intValue());
        assertThat(userAssigned.get("username")).isEqualTo("bob");

        // ----------------- UNASSIGN USER -----------------
        String unassignResponse = mockMvc.perform(delete("/tasks/" + taskId + "/users"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> unassignedTaskJson = mapper.readValue(unassignResponse, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        assertThat(unassignedTaskJson.get("assignedToId")).isNull();

        // ----------------- DELETE TASK -----------------
        mockMvc.perform(delete("/tasks/" + taskId))
                .andExpect(status().isOk());
        assertThat(taskRepository.findById(taskId)).isEmpty();
    }
}
