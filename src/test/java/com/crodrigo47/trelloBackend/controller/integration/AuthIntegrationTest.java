package com.crodrigo47.trelloBackend.controller.integration;

import com.crodrigo47.trelloBackend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;
    @Autowired UserRepository userRepository;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    void login_createsUserInDb() throws Exception {
        mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(Map.of("username", "fran"))))
            .andExpect(status().isOk());

        assertThat(userRepository.findByUsername("fran")).isPresent();
    }
}
