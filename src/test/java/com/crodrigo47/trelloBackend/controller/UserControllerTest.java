package com.crodrigo47.trelloBackend.controller;

import com.crodrigo47.trelloBackend.helper.Builders;
import com.crodrigo47.trelloBackend.model.User;
import com.crodrigo47.trelloBackend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;

    @MockBean UserService userService;

    @Test
    void getAllUsers_returnsJsonList() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(Builders.buildUserWithId("bob", 1L)));

        mockMvc.perform(get("/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].username").value("bob"));
    }

    @Test
    void getUserById_returnsUser() throws Exception {
        when(userService.getUserById(1L)).thenReturn(Optional.of(Builders.buildUserWithId("alice", 1L)));

        mockMvc.perform(get("/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void createUser_returnsCreatedUser() throws Exception {
        User saved = Builders.buildUserWithId("charlie", 2L);

        when(userService.createUser(any(User.class))).thenReturn(saved);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Builders.buildUser("charlie"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.username").value("charlie"));
    }

    @Test
    void updateUser_returnsUpdatedUser() throws Exception{
        User saved = Builders.buildUserWithId("bob", 1L);

        when(userService.updateUser(any(User.class))).thenReturn(saved);

        mockMvc.perform(put("/users/" + saved.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(saved)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(saved.getId()))
        .andExpect(jsonPath("$.username").value(saved.getUsername()));
    }

    @Test
    void deleteUser_callsService() throws Exception{
         Long userId = 1L;

        mockMvc.perform(delete("/users/" + userId))
            .andExpect(status().isOk());

        verify(userService).deleteUser(userId);
    }
}
