package io.github.erfangc.iam.authz.models;


public class DeleteRoleBindingResponse {

    private String message;
    private String timestamp;

    public String getMessage() {
        return this.message;
    }

    public DeleteRoleBindingResponse setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public DeleteRoleBindingResponse setTimestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

}