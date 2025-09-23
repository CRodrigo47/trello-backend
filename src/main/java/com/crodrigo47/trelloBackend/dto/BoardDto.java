package com.crodrigo47.trelloBackend.dto;

import java.util.Set;

public record BoardDto(
    Long id,
    String name,
    String description,
    Set<Long> userIds,
    Set<Long> taskIds
) {}
