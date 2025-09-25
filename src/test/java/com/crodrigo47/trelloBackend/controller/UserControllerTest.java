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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @MockBean BCryptPasswordEncoder passwordEncoder;

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
    void updateUser_returnsUpdatedUser() throws Exception {
        Map<String, String> body = Map.of(
            "username", "bobUpdated",
            "email", "bob@mail.com",
            "currentPassword", "bob"
        );

        // Usuario existente con username = "bob"
        User existingUser = Builders.buildUserWithId("bob", 1L);
        existingUser.setPassword(new BCryptPasswordEncoder().encode("bob"));

        // Usuario actualizado
        User updatedUser = Builders.buildUserWithId("bobUpdated", 1L);

        when(userService.getUserById(1L)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("bob", existingUser.getPassword())).thenReturn(true);
        when(userService.updateUser(any(User.class))).thenReturn(updatedUser);

        var expectedDto = BuildersDto.buildUserDtoWithId("bobUpdated", 1L);

    mockMvc.perform(put("/users/1")
            .principal(() -> "bob") // aquí simulas el usernameFromToken
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(expectedDto.id()))
            .andExpect(jsonPath("$.username").value(expectedDto.username()));
    }

    @Test
    void deleteUser_deletesOwnAccount() throws Exception {
        Long userId = 1L;
        User existingUser = Builders.buildUserWithId("bob", userId);
    
        when(userService.getUserById(userId)).thenReturn(Optional.of(existingUser));
    
        var userDetails = org.springframework.security.core.userdetails.User
                .withUsername("bob")
                .password("pass")
                .roles("USER")
                .build();
    
        var auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    
        mockMvc.perform(delete("/users/{id}", userId)
                .principal(auth)) // pasamos un Authentication real
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

    @Test
    void updateUser_wrongCurrentPassword_throwsInvalidPassword() throws Exception {
        Map<String, String> body = Map.of(
            "username", "bobUpdated",
            "currentPassword", "wrongPassword",
            "newPassword", "newSecret"
        );
    
        User existingUser = Builders.buildUserWithId("bob", 1L);
        existingUser.setPassword(new BCryptPasswordEncoder().encode("bob"));
    
        when(userService.getUserById(1L)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrongPassword", existingUser.getPassword())).thenReturn(false);
    
        mockMvc.perform(put("/users/1")
                .principal(() -> "bob")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().is4xxClientError());
    }

}
