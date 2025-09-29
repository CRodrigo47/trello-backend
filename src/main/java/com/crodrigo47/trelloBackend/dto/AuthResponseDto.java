package com.crodrigo47.trelloBackend.dto;

public record AuthResponseDto(
        Long id,
        String username,
        String token
) {}
