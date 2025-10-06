package com.crodrigo47.trelloBackend.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.crodrigo47.trelloBackend.dto.DtoMapper;
import com.crodrigo47.trelloBackend.dto.UserDto;
import com.crodrigo47.trelloBackend.dto.UserSearchDto;
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

    public UserController(UserService userService, BCryptPasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    // Solo ADMIN puede listar todos los usuarios
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

    // Obtener un usuario por ID (solo para sí mismo o ADMIN)
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

    @GetMapping("/search")
    public List<UserSearchDto> searchUsers(
            @RequestParam("username") String prefix,
            @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
            @AuthenticationPrincipal String username // para validar que hay un user autenticado
    ) {
        // opcional: validar que el caller existe
        userService.getUserByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("Current user not found"));

        // delegar al service
        return userService.searchUsersByPrefix(prefix, limit);
    }

    // Actualizar el perfil del usuario actual
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
            user.setPassword(body.get("newPassword")); // Codificación en el service
        }

        return DtoMapper.toUserDto(userService.updateUser(user));
    }

    // Eliminar la propia cuenta
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
