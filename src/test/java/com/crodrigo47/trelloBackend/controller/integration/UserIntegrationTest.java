package com.crodrigo47.trelloBackend.controller.integration;

import com.crodrigo47.trelloBackend.helper.Builders;
import com.crodrigo47.trelloBackend.model.User;
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
class UserIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;
    @Autowired UserRepository userRepository;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    void userFullFlow_crud() throws Exception {
        // ----------------- CREATE -----------------
        String userName = "alice";
        String updatedUserName = "aliceUpdated";

        User user = Builders.buildUser(userName);
        String createResponse = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> userJson = mapper.readValue(createResponse, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        Long userId = Long.valueOf((Integer) userJson.get("id"));
        assertThat(userJson.get("username")).isEqualTo(userName);

        // ----------------- UPDATE -----------------
        userJson.put("username", updatedUserName);
        userJson.put("password", "dummyPassword");
        String updateResponse = mockMvc.perform(put("/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userJson)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> updatedUserJson = mapper.readValue(updateResponse, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        assertThat(updatedUserJson.get("username")).isEqualTo(updatedUserName);

        // ----------------- GET -----------------
        mockMvc.perform(get("/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value(updatedUserName));

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
