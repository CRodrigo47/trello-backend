package com.crodrigo47.trelloBackend.dto;

import java.util.Set;

import com.crodrigo47.trelloBackend.model.User.Role;

public record UserDto(
    Long id,
    String username,
    Role role,
    Set<Long> boardIds,
    Set<Long> taskIds
) {}
