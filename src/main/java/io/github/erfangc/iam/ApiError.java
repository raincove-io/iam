package io.github.erfangc.iam;

public class ApiError {
    private String id;
    private String message;
    private String timestamp;

    public String getMessage() {
        return message;
    }

    public ApiError setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public ApiError setTimestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getId() {
        return id;
    }

    public ApiError setId(String id) {
        this.id = id;
        return this;
    }
}
