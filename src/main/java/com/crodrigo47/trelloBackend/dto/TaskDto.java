package com.crodrigo47.trelloBackend.dto;

import java.time.LocalDateTime;

public record TaskDto(
    Long id,
    String title,
    String description,
    String status,
    Long assignedToId,
    Long boardId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
