package com.jasperstudio.descriptor;

import java.time.LocalDateTime;

public class LogEntry {
    private final LocalDateTime timestamp;
    private final String message;
    private final String description; // Additional details (e.g. stack trace)

    public LogEntry(String message, String description) {
        this.timestamp = LocalDateTime.now();
        this.message = message;
        this.description = description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return timestamp + ": " + message;
    }
}
