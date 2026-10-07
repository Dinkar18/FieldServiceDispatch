package com.example.dispatch.exception;

public class ErrorResponse {
    private String code;
    private String message;
    private String timestamp;

    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
        this.timestamp = java.time.Instant.now().toString();
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
    public String getTimestamp() { return timestamp; }
}
