package com.crodrigo47.trelloBackend.controller;

import com.crodrigo47.trelloBackend.model.User;
import com.crodrigo47.trelloBackend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
    "jwt.secret=supersecuretestkeythatisatleast32bytes!",
    "jwt.expiration-ms=3600000"
})
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;

    @MockBean UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void login_returnsExistingUser() throws Exception {
        String rawPassword = "V4E8r9dYp2sM6aT1qW3kZx7nBbC0L2fH"; // debe ser >=32 chars
        String encodedPassword = passwordEncoder.encode(rawPassword);

        when(userRepository.findByUsername("dave"))
                .thenReturn(Optional.of(User.builder()
                        .id(1L)
                        .username("dave")
                        .password(encodedPassword)
                        .build()));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"dave\", \"password\": \"" + rawPassword + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("dave"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_createsUserIfNotExists() throws Exception {
        when(userRepository.findByUsername("eve")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User u = invocation.getArgument(0);
                    u.setId(99L);
                    return u;
                });

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"eve\", \"password\": \"mypassword\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }
}
