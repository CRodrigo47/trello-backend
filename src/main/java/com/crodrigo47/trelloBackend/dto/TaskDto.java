package com.crodrigo47.trelloBackend.dto;

import java.time.LocalDateTime;

import com.crodrigo47.trelloBackend.model.Task.Status;

public record TaskDto(
    Long id,
    String title,
    String description,
    Status status,
    Long assignedToId,
    Long createdById,
    Long boardId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
