package io.github.erfangc.iam.authz.models;


public class CreateOrUpdateBindingResponse {

    private String message;
    private String timestamp;

    public String getMessage() {
        return this.message;
    }

    public CreateOrUpdateBindingResponse setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public CreateOrUpdateBindingResponse setTimestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

}