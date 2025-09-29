package com.crodrigo47.trelloBackend.controller;

import com.crodrigo47.trelloBackend.config.JwtUtil;
import com.crodrigo47.trelloBackend.dto.AuthResponseDto;
import com.crodrigo47.trelloBackend.exception.InvalidPasswordException;
import com.crodrigo47.trelloBackend.model.User;
import com.crodrigo47.trelloBackend.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository,
                          BCryptPasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public AuthResponseDto login(@RequestBody User requestUser) {
        User user = userRepository.findByUsername(requestUser.getUsername())
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .username(requestUser.getUsername())
                            .password(passwordEncoder.encode(requestUser.getPassword()))
                            .role(User.Role.USER)
                            .build();
                    return userRepository.save(newUser);
                });

        if (!passwordEncoder.matches(requestUser.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Usuario existente o contraseña incorrecta");
        }

        String token = jwtUtil.generateToken(new com.crodrigo47.trelloBackend.config.CustomUserDetails(user));

        return new AuthResponseDto(user.getId(), user.getUsername(), token);
    }

}
