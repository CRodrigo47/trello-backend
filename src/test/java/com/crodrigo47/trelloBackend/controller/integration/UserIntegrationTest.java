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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    @Autowired BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    void userFullFlow_crud_withoutCreate() throws Exception {
        String username = "alice";
        String updatedUsername = "aliceUpdated";

        // ----------------- SETUP: CREAR USUARIO DIRECTAMENTE (con password codificada) -----------------
        User user = Builders.buildUser(username);
        user.setPassword(passwordEncoder.encode(username)); // importante: codificar la password para que matches() funcione
        user = userRepository.save(user); // guardamos en DB directamente
        Long userId = user.getId();

        // ----------------- PREPARAR UN ADMIN PARA LA PETICIÓN DE LISTADO -----------------
        User admin = Builders.buildUser("admin");
        admin.setRole(User.Role.ADMIN);
        admin.setPassword(passwordEncoder.encode("admin"));
        admin = userRepository.save(admin);

        // ----------------- UPDATE -----------------
        Map<String, String> updateBody = Map.of(
            "username", updatedUsername,
            "currentPassword", username // coincide con la password original no codificada
        );

        String updateResp = mockMvc.perform(put("/users/" + userId)
                        .principal(new UsernamePasswordAuthenticationToken(username, null)) // principal = nombre antiguo
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateBody)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        @SuppressWarnings("unchecked")
        Map<String, Object> updatedUserJson = mapper.readValue(updateResp, Map.class);
        assertThat(updatedUserJson.get("username")).isEqualTo(updatedUsername);

        // ----------------- GET (con principal del mismo usuario actualizado) -----------------
        mockMvc.perform(get("/users/" + userId)
                .principal(new UsernamePasswordAuthenticationToken(updatedUsername, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value(updatedUsername));

        // ----------------- LIST (requiere ADMIN) -----------------
        mockMvc.perform(get("/users")
                .principal(new UsernamePasswordAuthenticationToken(admin.getUsername(), null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(userId));

        // ----------------- DELETE (principal = usuario actualizado) -----------------
        mockMvc.perform(delete("/users/" + userId)
                .principal(new UsernamePasswordAuthenticationToken(updatedUsername, null)))
                .andExpect(status().isOk());

        assertThat(userRepository.findById(userId)).isEmpty();
    }
}
