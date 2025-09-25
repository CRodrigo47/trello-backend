package com.crodrigo47.trelloBackend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crodrigo47.trelloBackend.dto.DtoMapper;
import com.crodrigo47.trelloBackend.dto.UserDto;
import com.crodrigo47.trelloBackend.exception.InvalidPasswordException;
import com.crodrigo47.trelloBackend.exception.UserNotFoundException;
import com.crodrigo47.trelloBackend.model.User;
import com.crodrigo47.trelloBackend.service.UserService;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/users")
public class UserController {

    private final BCryptPasswordEncoder passwordEncoder;
    
    private final UserService userService;

    public UserController(UserService userService, BCryptPasswordEncoder passwordEncoder){
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers().stream()
            .map(DtoMapper::toUserDto)
            .toList();
    }
    
    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
            .map(DtoMapper::toUserDto)
            .orElseThrow(() -> new UserNotFoundException("User id " + id + " not found"));
    }
    
    @PutMapping("/{id}")
    public UserDto updateUser(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Principal principal
    ) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new UserNotFoundException("User id " + id + " not found"));

        String usernameFromToken = principal.getName();

        if (!user.getUsername().equals(usernameFromToken)) {
            throw new org.springframework.security.access.AccessDeniedException("You can only update your own profile");
        }

        if (body.containsKey("username")) user.setUsername(body.get("username"));
        if (body.containsKey("email")) user.setEmail(body.get("email"));

        if (body.containsKey("newPassword")) {
            String currentPassword = body.get("currentPassword");
            if (currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPassword())) {
                throw new InvalidPasswordException("Current password is incorrect");
            }
            user.setPassword(passwordEncoder.encode(body.get("newPassword")));
        }

        return DtoMapper.toUserDto(userService.updateUser(user));
    }

    
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id, Principal principal) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new UserNotFoundException("User id " + id + " not found"));
    
        // Comprobamos que el usuario loggeado sea el mismo que quiere borrar
        if (!user.getUsername().equals(principal.getName())) {
            throw new AccessDeniedException("You can only delete your own account");
        }
    
        userService.deleteUser(id);
    }
    
    
}
