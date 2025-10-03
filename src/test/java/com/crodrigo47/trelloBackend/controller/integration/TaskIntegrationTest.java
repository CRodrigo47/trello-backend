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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
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
    @Autowired com.crodrigo47.trelloBackend.repository.TaskRepository taskRepository;
    @Autowired com.crodrigo47.trelloBackend.repository.BoardRepository boardRepository;
    @Autowired com.crodrigo47.trelloBackend.repository.UserRepository userRepository;

    @BeforeEach
    void setup() {
        taskRepository.deleteAll();
        boardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        // limpiar contexto de seguridad por si acaso
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void taskFullFlow_crudAndRelations() throws Exception {
        // ----------------- SETUP: crear usuario y board -----------------
        User testCreator = Builders.buildUser("CreatorTest");
        testCreator = userRepository.save(testCreator);

        var testBoard = boardRepository.save(Builders.buildBoard("BoardTest", testCreator));

        // IMPORTANTE: añadir explícitamente el creador como miembro del board
        // (cuando creas el board vía controller su lógica añade al creador, pero
        // al guardarlo directamente con el builder no queda en board.getUsers()).
        testBoard.addUser(testCreator);
        testBoard = boardRepository.save(testBoard);

        // setear SecurityContext con el usuario autenticado (required para @AuthenticationPrincipal)
        var auth = new UsernamePasswordAuthenticationToken(testCreator, null);
        org.springframework.security.core.context.SecurityContextHolder.setContext(new SecurityContextImpl(auth));

        try {
            // ----------------- CREATE TASK -----------------
            Map<String, Object> createBody = Map.of(
                    "title", "TestTask",
                    "board", Map.of("id", testBoard.getId())
            );

            String createResponse = mockMvc.perform(post("/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(createBody)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            Map<String, Object> taskJson = mapper.readValue(createResponse, new TypeReference<Map<String, Object>>() {});
            Number tmpId = (Number) taskJson.get("id");
            Long taskId = tmpId.longValue();
            assertThat(taskJson.get("title")).isEqualTo("TestTask");

            // ----------------- UPDATE TASK -----------------
            Map<String, Object> updateBody = Map.of("title", "UpdatedTask");

            String updateResponse = mockMvc.perform(put("/tasks/" + taskId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(updateBody)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            Map<String, Object> updatedTaskJson = mapper.readValue(updateResponse, new TypeReference<Map<String, Object>>() {});
            assertThat(updatedTaskJson.get("title")).isEqualTo("UpdatedTask");

            // ----------------- GET TASK -----------------
            mockMvc.perform(get("/tasks/" + taskId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(taskId))
                    .andExpect(jsonPath("$.title").value("UpdatedTask"));

            // ----------------- ASSIGN USER -----------------
            String assignResponse = mockMvc.perform(post("/tasks/" + taskId + "/users/" + testCreator.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            Map<String, Object> assignedTaskJson = mapper.readValue(assignResponse, new TypeReference<Map<String, Object>>() {});

            // comprobación defensiva: puede venir assignedToId (número) o assignedTo { id: ... }
            Object assignedIdObj = assignedTaskJson.getOrDefault("assignedToId", assignedTaskJson.get("assignedTo"));
            if (assignedIdObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> assignedMap = (Map<String, Object>) assignedIdObj;
                Number assignedIdNumber = (Number) assignedMap.get("id");
                assertThat(assignedIdNumber.longValue()).isEqualTo(testCreator.getId());
            } else if (assignedIdObj instanceof Number) {
                Number n = (Number) assignedIdObj;
                assertThat(n.longValue()).isEqualTo(testCreator.getId());
            } else {
                // fallback: si la estructura es diferente, chequeamos que assignedTo exista
                assertThat(assignedTaskJson.get("assignedTo")).isNotNull();
            }

            // ----------------- GET USER ASSIGNED -----------------
            String getUserResp = mockMvc.perform(get("/tasks/" + taskId + "/users")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            Map<String, Object> userAssigned = mapper.readValue(getUserResp, new TypeReference<Map<String, Object>>() {});
            Number returnedUserId = (Number) userAssigned.get("id");
            assertThat(returnedUserId.longValue()).isEqualTo(testCreator.getId().longValue());
            assertThat(userAssigned.get("username")).isEqualTo(testCreator.getUsername());

            // ----------------- UNASSIGN USER -----------------
            String unassignResponse = mockMvc.perform(delete("/tasks/" + taskId + "/users"))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            Map<String, Object> unassignedTaskJson = mapper.readValue(unassignResponse, new TypeReference<Map<String, Object>>() {});
            Object assignedAfter = unassignedTaskJson.getOrDefault("assignedToId", unassignedTaskJson.get("assignedTo"));
            assertThat(assignedAfter).isNull();

            // ----------------- DELETE TASK -----------------
            mockMvc.perform(delete("/tasks/" + taskId))
                    .andExpect(status().isOk());

            assertThat(taskRepository.findById(taskId)).isEmpty();
        } finally {
            // limpiamos siempre el contexto de seguridad
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }
}
