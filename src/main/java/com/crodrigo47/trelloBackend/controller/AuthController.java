package com.crodrigo47.trelloBackend.controller;

import org.springframework.web.bind.annotation.*;

import com.crodrigo47.trelloBackend.model.User;
import com.crodrigo47.trelloBackend.repository.UserRepository;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Login muy básico
    @PostMapping("/login")
    public User login(@RequestBody User requestUser) {
        return userRepository.findByUsername(requestUser.getUsername())
                .orElseGet(() -> userRepository.save(User.builder()
                        .username(requestUser.getUsername())
                        .build()));
    }
}
