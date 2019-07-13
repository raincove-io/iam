package io.github.erfangc.iam.authz.models;


public class CreateOrUpdateRoleBindingResponse {

    private String message;
    private String timestamp;

    public String getMessage() {
        return this.message;
    }

    public CreateOrUpdateRoleBindingResponse setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public CreateOrUpdateRoleBindingResponse setTimestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

}