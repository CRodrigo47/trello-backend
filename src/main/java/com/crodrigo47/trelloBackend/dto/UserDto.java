package com.crodrigo47.trelloBackend.dto;

import java.util.Set;

public record UserDto(
    Long id,
    String username,
    String email,
    String token,
    Set<Long> boardIds,
    Set<Long> taskIds
) {}
