package io.github.erfangc.iam.authz.models;


public class CreateOrUpdateRoleResponse {

    private String message;
    private String timestamp;

    public String getMessage() {
        return this.message;
    }

    public CreateOrUpdateRoleResponse setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public CreateOrUpdateRoleResponse setTimestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

}