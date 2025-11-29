package com.mareen.bookme.dto.response;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        Map<String, String> errors
) {

    // Constructor for simple errors (no field errors)
    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message) {
        this(timestamp, status, error, message, Collections.emptyMap());
    }
}
