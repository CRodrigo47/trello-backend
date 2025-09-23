package com.crodrigo47.trelloBackend.controller.integration;

import com.crodrigo47.trelloBackend.helper.Builders;
import com.crodrigo47.trelloBackend.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
class UserIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;
    @Autowired com.crodrigo47.trelloBackend.repository.UserRepository userRepository;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    void userFullFlow_crud() throws Exception {
        String username = "alice";
        String updatedUsername = "aliceUpdated";

        // ----------------- CREATE -----------------
        User user = Builders.buildUser(username);
        String createResp = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        @SuppressWarnings("unchecked")
        Map<String, Object> userJson = mapper.readValue(createResp, Map.class);
        Long userId = Long.valueOf((Integer) userJson.get("id"));
        assertThat(userJson.get("username")).isEqualTo(username);

        // ----------------- UPDATE -----------------
        Map<String, String> updateBody = Map.of(
            "username", updatedUsername,
            "currentPassword", username // coincidir con la password original
        );

        String updateResp = mockMvc.perform(put("/users/" + userId)
                        .principal(new UsernamePasswordAuthenticationToken(username, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateBody)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        @SuppressWarnings("unchecked")
        Map<String, Object> updatedUserJson = mapper.readValue(updateResp, Map.class);
        assertThat(updatedUserJson.get("username")).isEqualTo(updatedUsername);

        // ----------------- GET -----------------
        mockMvc.perform(get("/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value(updatedUsername));

        // ----------------- LIST -----------------
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(userId));

        // ----------------- DELETE -----------------
        mockMvc.perform(delete("/users/" + userId))
                .andExpect(status().isOk());

        assertThat(userRepository.findById(userId)).isEmpty();
    }
}
