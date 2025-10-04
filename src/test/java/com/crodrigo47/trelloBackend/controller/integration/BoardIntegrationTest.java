package com.crodrigo47.trelloBackend.controller.integration;

import com.crodrigo47.trelloBackend.helper.Builders;
import com.crodrigo47.trelloBackend.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
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
    @Autowired com.crodrigo47.trelloBackend.repository.BoardRepository boardRepository;
    @Autowired com.crodrigo47.trelloBackend.repository.UserRepository userRepository;
    @Autowired com.crodrigo47.trelloBackend.repository.TaskRepository taskRepository;

    @BeforeEach
    void setup() {
        taskRepository.deleteAll();
        boardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void boardFullFlow_crudAndRelations() throws Exception {
        // ----------------- PREPARAR USUARIO (creator) -----------------
        User user = Builders.buildUser("bob");
        User creator = userRepository.save(user);

        // NOTA: inyectamos el principal en cada request con .principal(...)
        try {
            // ----------------- CREATE -----------------
            String boardName = "IntegrationBoard";
            String updatedBoardName = "UpdatedBoard";

            String createResponse = mockMvc.perform(post("/boards")
                    .principal(() -> creator.getUsername())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(Map.of("name", boardName))))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            Map<String, Object> boardJson = mapper.readValue(createResponse, new TypeReference<Map<String, Object>>() {});
            Number tmpId = (Number) boardJson.get("id");
            Long boardId = tmpId.longValue();
            assertThat(boardJson.get("name")).isEqualTo(boardName);
            assertThat(boardRepository.findById(boardId)).isPresent();

            // ----------------- UPDATE -----------------
            boardJson.put("name", updatedBoardName);
            String updateResponse = mockMvc.perform(put("/boards/" + boardId)
                    .principal(() -> creator.getUsername())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(boardJson)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            Map<String, Object> updatedBoardJson = mapper.readValue(updateResponse, new TypeReference<Map<String, Object>>() {});
            assertThat(updatedBoardJson.get("name")).isEqualTo(updatedBoardName);

            // ----------------- ADD USER -----------------
            User boardUser = userRepository.save(Builders.buildUser("alice"));

            String addUserResponse = mockMvc.perform(post("/boards/" + boardId + "/users/" + boardUser.getId())
                    .principal(() -> creator.getUsername())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            Map<String, Object> boardWithUserJson = mapper.readValue(addUserResponse, new TypeReference<Map<String, Object>>() {});
            @SuppressWarnings("unchecked")
            List<Object> userIds = (List<Object>) boardWithUserJson.get("userIds");
            assertThat(userIds).contains(boardUser.getId().intValue());

            // ----------------- ADD TASK (creada por el endpoint) -----------------
            String taskTitle = "task1";
            String addTaskResponse = mockMvc.perform(post("/boards/" + boardId + "/tasks")
                    .principal(() -> creator.getUsername())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(Map.of("title", taskTitle))))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            Map<String, Object> boardWithTaskJson = mapper.readValue(addTaskResponse, new TypeReference<Map<String, Object>>() {});
            @SuppressWarnings("unchecked")
            List<Object> taskIds = (List<Object>) boardWithTaskJson.get("taskIds");
            assertThat(taskIds).isNotEmpty();

            // coger el id creado para comparar más abajo
            Integer addedTaskId = (Integer) taskIds.get(0);

            // ----------------- GET TASKS -----------------
            mockMvc.perform(get("/boards/" + boardId + "/tasks").principal(() -> creator.getUsername()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(addedTaskId))
                    .andExpect(jsonPath("$[0].title").value(taskTitle));

            // ----------------- GET USERS -----------------
            mockMvc.perform(get("/boards/" + boardId + "/users").principal(() -> creator.getUsername()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(creator.getId()))
                    .andExpect(jsonPath("$[0].username").value(creator.getUsername()));

            // ----------------- REMOVE USER -----------------
            mockMvc.perform(delete("/boards/" + boardId + "/users/" + boardUser.getId())
                    .principal(() -> creator.getUsername()))
                    .andExpect(status().isOk());

            // ----------------- REMOVE TASK -----------------
            mockMvc.perform(delete("/boards/" + boardId + "/tasks/" + addedTaskId)
                    .principal(() -> creator.getUsername()))
                    .andExpect(status().isOk());

            // ----------------- DELETE BOARD -----------------
            mockMvc.perform(delete("/boards/" + boardId).principal(() -> creator.getUsername()))
                    .andExpect(status().isOk());

            assertThat(boardRepository.findById(boardId)).isEmpty();
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }
}
