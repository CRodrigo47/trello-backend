package com.crodrigo47.trelloBackend.controller;

import com.crodrigo47.trelloBackend.helper.Builders;
import com.crodrigo47.trelloBackend.helper.BuildersDto;
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
        User user = Builders.buildUserWithId("bob", 1L);
        when(userService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(user.getId()))
            .andExpect(jsonPath("$[0].username").value(user.getUsername()));
    }

    @Test
    void getUserById_returnsUser() throws Exception {
        User user = Builders.buildUserWithId("alice", 1L);
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(user.getId()))
            .andExpect(jsonPath("$.username").value(user.getUsername()));
    }

    @Test
    void createUser_returnsCreatedUser() throws Exception {
        User inputUser = Builders.buildUser("charlie");
        User savedUser = Builders.buildUserWithId("charlie", 2L);

        when(userService.createUser(any(User.class))).thenReturn(savedUser);

        var expectedDto = BuildersDto.buildUserDtoWithId("charlie", 2L);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(inputUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedDto.id()))
                .andExpect(jsonPath("$.username").value(expectedDto.username()));
    }

    @Test
    void updateUser_returnsUpdatedUser() throws Exception {
        User updated = Builders.buildUserWithId("bob", 1L);
        when(userService.updateUser(any(User.class))).thenReturn(updated);

        var expectedDto = BuildersDto.buildUserDtoWithId("bob", 1L);

        mockMvc.perform(put("/users/" + updated.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedDto.id()))
                .andExpect(jsonPath("$.username").value(expectedDto.username()));
    }

    @Test
    void deleteUser_callsService() throws Exception {
        Long userId = 1L;

        mockMvc.perform(delete("/users/" + userId))
            .andExpect(status().isOk());

        verify(userService).deleteUser(userId);
    }

    //-------------------------------ERROR TEST----------------------------------------//

    @Test
    void getUserById_notFound_returns404() throws Exception {
        when(userService.getUserById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/users/1"))
            .andExpect(status().isNotFound());
    }
}
