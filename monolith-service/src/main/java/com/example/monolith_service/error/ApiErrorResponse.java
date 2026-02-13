package com.example.monolith_service.error;

import java.time.Instant;
import java.util.Map;

public class ApiErrorResponse {

    private final String code;
    private final String message;
    private final Instant timestamp;
    private final String path;
    private final Map<String, String> fieldErrors;

    public ApiErrorResponse(String code, String message, String path, Map<String, String> fieldErrors) {
        this.code = code;
        this.message = message;
        this.path = path;
        this.fieldErrors = fieldErrors;
        this.timestamp = Instant.now();
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
