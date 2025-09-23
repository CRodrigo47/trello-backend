package com.crodrigo47.trelloBackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Desactivamos CSRF porque estamos haciendo peticiones API (Postman, HTTPie)
            .csrf(csrf -> csrf.disable())

            // Configuramos qué endpoints permiten acceso sin token
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/login").permitAll() // Login sin JWT
                .anyRequest().authenticated()               // resto requiere autenticación
            );

        return http.build();
    }
}
