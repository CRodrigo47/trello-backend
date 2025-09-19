package com.crodrigo47.trelloBackend.controller;

import com.crodrigo47.trelloBackend.model.User;
import com.crodrigo47.trelloBackend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;

    @MockBean UserRepository userRepository;

    @Test
    void login_returnsExistingUser() throws Exception {
        when(userRepository.findByUsername("dave"))
            .thenReturn(Optional.of(User.builder().id(1L).username("dave").build()));

        mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\": \"dave\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("dave"));
    }

    @Test
    void login_createsUserIfNotExists() throws Exception {
        when(userRepository.findByUsername("eve")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class)))
            .thenReturn(User.builder().id(99L).username("eve").build());

        mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\": \"eve\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(99));
    }
}
