package com.careerpilot.common.payload;

import java.time.Instant;
import java.util.List;

public class ErrorDetails {

    private Instant timestamp;
    private String message;
    private List<String> details;

    public ErrorDetails() {
        this.timestamp = Instant.now();
    }

    public ErrorDetails(String message, List<String> details) {
        this.timestamp = Instant.now();
        this.message = message;
        this.details = details;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getDetails() {
        return details;
    }

    public void setDetails(List<String> details) {
        this.details = details;
    }
}

