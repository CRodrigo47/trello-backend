package com.crodrigo47.trelloBackend.controller.integration;

import com.crodrigo47.trelloBackend.config.JwtUtil;
import com.crodrigo47.trelloBackend.model.User;
import com.crodrigo47.trelloBackend.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
    "jwt.secret=${JWT_TEST_SECRET:supersecuretestkeythatisatleast32bytes!}",
    "jwt.expiration-ms=3600000"
})
class AuthIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;
    @Autowired UserRepository userRepository;
    @Autowired JwtUtil jwtUtil;
    @Autowired BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    void login_createsUserInDb() throws Exception {
        String username = "fran";
        String password = "supersecret";

        String response = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(
                        "username", username,
                        "password", password
                ))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> jsonResponse = mapper.readValue(response, new TypeReference<Map<String, Object>>() {});
        assertThat(jsonResponse.get("username")).isEqualTo(username);
        assertThat(jsonResponse.get("token")).isNotNull();
        assertThat(jsonResponse.get("id")).isNotNull();

        String token = (String) jsonResponse.get("token");

        // Verificamos que el token realmente contiene el username
        assertThat(jwtUtil.extractUsername(token)).isEqualTo(username);

        // Verificamos que el usuario se creó en BD y la password está codificada
        Optional<User> userOpt = userRepository.findByUsername(username);
        assertThat(userOpt).isPresent();
        User saved = userOpt.get();

        // La password guardada no es la plain text
        assertThat(saved.getPassword()).isNotEqualTo(password);

        // Y además el encoder coincide (matches == true)
        assertThat(passwordEncoder.matches(password, saved.getPassword())).isTrue();

        // El id devuelto en la respuesta debe ser el mismo que el de la entidad persistida
        Number returnedId = (Number) jsonResponse.get("id");
        assertThat(returnedId.longValue()).isEqualTo(saved.getId());
    }
}
