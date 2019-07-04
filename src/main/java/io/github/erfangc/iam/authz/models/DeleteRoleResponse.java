package io.github.erfangc.iam.authz.models;


public class DeleteRoleResponse {

    private String message;
    private String timestamp;

    public String getMessage() {
        return this.message;
    }

    public DeleteRoleResponse setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public DeleteRoleResponse setTimestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

}