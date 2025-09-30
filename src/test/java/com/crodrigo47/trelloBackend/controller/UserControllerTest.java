package com.crodrigo47.trelloBackend.controller;

import com.crodrigo47.trelloBackend.config.JwtAuthenticationFilter;
import com.crodrigo47.trelloBackend.dto.DtoMapper;
import com.crodrigo47.trelloBackend.dto.UserDto;
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

import java.security.Principal;
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
    @MockBean UserService userService;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;

    //--------------------------SUCCESS TESTS CORREGIDOS--------------------------//

    @Test
    void getAllUsers_returnsJsonList() throws Exception {
        User admin = User.builder().id(1L).username("admin").role(User.Role.ADMIN).build();
        when(userService.getAllUsers()).thenReturn(List.of(admin));
        when(userService.getUserByUsername("admin")).thenReturn(Optional.of(admin));

        Principal principal = new UsernamePasswordAuthenticationToken("admin", null);

        mockMvc.perform(get("/users").principal(principal))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(admin.getId()))
            .andExpect(jsonPath("$[0].username").value(admin.getUsername()));
    }

    @Test
    void getUserById_returnsUser() throws Exception {
        User user = User.builder().id(1L).username("alice").role(User.Role.USER).build();
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));
        when(userService.getUserByUsername("alice")).thenReturn(Optional.of(user));

        UserDto expectedDto = DtoMapper.toUserDto(user);
        Principal principal = new UsernamePasswordAuthenticationToken("alice", null);

        mockMvc.perform(get("/users/1").principal(principal))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(expectedDto.id()))
            .andExpect(jsonPath("$.username").value(expectedDto.username()));
    }

    @Test
    void updateUser_returnsUpdatedUser() throws Exception {
        Map<String, String> body = Map.of(
            "username", "bobUpdated",
            "currentPassword", "bob",
            "newPassword", "newSecret"
        );

        User existingUser = User.builder().id(1L).username("bob").password("encodedPassword").build();
        User updatedUser = User.builder().id(1L).username("bobUpdated").build();

        when(userService.getUserById(1L)).thenReturn(Optional.of(existingUser));
        when(userService.getUserByUsername("bob")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("bob", existingUser.getPassword())).thenReturn(true);
        when(userService.updateUser(any(User.class))).thenReturn(updatedUser);

        UserDto expectedDto = DtoMapper.toUserDto(updatedUser);
        Principal principal = new UsernamePasswordAuthenticationToken("bob", null);

        mockMvc.perform(put("/users/1")
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedDto.id()))
                .andExpect(jsonPath("$.username").value(expectedDto.username()));
    }

    @Test
    void deleteUser_deletesOwnAccount() throws Exception {
        Long userId = 1L;
        User existingUser = User.builder().id(userId).username("bob").build();

        when(userService.getUserById(userId)).thenReturn(Optional.of(existingUser));
        when(userService.getUserByUsername("bob")).thenReturn(Optional.of(existingUser));

        Principal principal = new UsernamePasswordAuthenticationToken("bob", null);

        mockMvc.perform(delete("/users/{id}", userId)
                .principal(principal))
            .andExpect(status().isOk());

        verify(userService).deleteUser(userId);
    }

    //--------------------------ERROR TESTS--------------------------//

    @Test
    void getUserById_notFound_returns404() throws Exception {
        when(userService.getUserById(1L)).thenReturn(Optional.empty());

        Principal principal = () -> "alice";

        mockMvc.perform(get("/users/1").principal(principal))
            .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_wrongCurrentPassword_throwsInvalidPassword() throws Exception {
        Map<String, String> body = Map.of(
            "username", "bobUpdated",
            "currentPassword", "wrongPassword",
            "newPassword", "newSecret"
        );

        User existingUser = User.builder().id(1L).username("bob").password("encodedPassword").build();

        when(userService.getUserById(1L)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrongPassword", existingUser.getPassword())).thenReturn(false);

        Principal principal = new UsernamePasswordAuthenticationToken("bob", null);

        mockMvc.perform(put("/users/1")
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().is4xxClientError());
    }

}
