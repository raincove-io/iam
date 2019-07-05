package io.github.erfangc.iam;

import org.springframework.http.HttpStatus;

import java.time.Instant;

public class ApiException extends RuntimeException {
    private HttpStatus httpStatus;
    private String message = "An error has occurred";
    private Instant timestamp = Instant.now();

    public ApiException() {
    }

    public ApiException(Throwable cause) {
        super(cause);
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public ApiException setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public ApiException setMessage(String message) {
        this.message = message;
        return this;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public ApiException setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }

}
