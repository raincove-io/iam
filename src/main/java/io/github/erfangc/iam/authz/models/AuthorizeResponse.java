package io.github.erfangc.iam.authz.models;


public class AuthorizeResponse {

    private Boolean allowed;
    private String message;
    private String timestamp;

    public Boolean getAllowed() {
        return this.allowed;
    }

    public AuthorizeResponse setAllowed(Boolean allowed) {
        this.allowed = allowed;
        return this;
    }

    public String getMessage() {
        return this.message;
    }

    public AuthorizeResponse setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public AuthorizeResponse setTimestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

}