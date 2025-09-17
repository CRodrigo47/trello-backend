package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

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
