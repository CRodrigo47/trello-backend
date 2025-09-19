package com.crodrigo47.trelloBackend.exception;

import java.time.Instant;

public record ErrorResponse(int status, String error, String message, Instant timestamp) {}
