package com.crodrigo47.trelloBackend.controller;

import com.crodrigo47.trelloBackend.model.User;
import com.crodrigo47.trelloBackend.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Key;
import java.util.Date;


@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public User login(@RequestBody User requestUser) {
        User user = userRepository.findByUsername(requestUser.getUsername())
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .username(requestUser.getUsername())
                            .password(passwordEncoder.encode(requestUser.getPassword()))
                            .build();
                    return userRepository.save(newUser);
                });

        if (!passwordEncoder.matches(requestUser.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        String token = Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        user.setToken(token);
        return user;
    }
}
