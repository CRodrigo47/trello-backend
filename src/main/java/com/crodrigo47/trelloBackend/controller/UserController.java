package com.crodrigo47.trelloBackend.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.crodrigo47.trelloBackend.dto.DtoMapper;
import com.crodrigo47.trelloBackend.dto.UserDto;
import com.crodrigo47.trelloBackend.exception.InvalidPasswordException;
import com.crodrigo47.trelloBackend.exception.UserNotFoundException;
import com.crodrigo47.trelloBackend.model.User;
import com.crodrigo47.trelloBackend.service.UserService;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final BCryptPasswordEncoder passwordEncoder;
    private final UserService userService;

    public UserController(UserService userService, BCryptPasswordEncoder passwordEncoder){
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    // Solo ADMIN puede listar todos
    @GetMapping
    public List<UserDto> getAllUsers(Principal principal) {
        User current = userService.getUserByUsername(principal.getName())
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));

        if (current.getRole() != User.Role.ADMIN) {
            throw new AccessDeniedException("Only ADMIN can access all users");
        }

        return userService.getAllUsers().stream()
                .map(DtoMapper::toUserDto)
                .toList();
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id, Principal principal) {
        User current = userService.getUserByUsername(principal.getName())
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));

        User user = userService.getUserById(id)
                .orElseThrow(() -> new UserNotFoundException("User id " + id + " not found"));

        if (current.getRole() != User.Role.ADMIN && !current.getId().equals(user.getId())) {
            throw new AccessDeniedException("You can only see your own profile");
        }

        return DtoMapper.toUserDto(user);
    }

    @PutMapping("/{id}")
    public UserDto updateUser(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Principal principal
    ) {
        User current = userService.getUserByUsername(principal.getName())
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));

        User user = userService.getUserById(id)
                .orElseThrow(() -> new UserNotFoundException("User id " + id + " not found"));

        if (!current.getId().equals(user.getId())) {
            throw new AccessDeniedException("You can only update your own profile");
        }

        if (body.containsKey("username")) user.setUsername(body.get("username"));

        if (body.containsKey("newPassword")) {
            String currentPassword = body.get("currentPassword");
            if (currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPassword())) {
                throw new InvalidPasswordException("Current password is incorrect");
            }
            user.setPassword(body.get("newPassword")); // Codificación se hace en service
        }

        return DtoMapper.toUserDto(userService.updateUser(user));
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id, Principal principal) {
        User current = userService.getUserByUsername(principal.getName())
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));

        if (!current.getId().equals(id)) {
            throw new AccessDeniedException("You can only delete your own account");
        }

        userService.deleteUser(id);
    }
}
