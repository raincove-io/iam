package io.github.erfangc.iam.authz.services;

public class AccessRequest {
    private String resource;
    private String action;
    private String sub;

    public String getResource() {
        return resource;
    }

    public AccessRequest setResource(String resource) {
        this.resource = resource;
        return this;
    }

    public String getAction() {
        return action;
    }

    public AccessRequest setAction(String action) {
        this.action = action;
        return this;
    }

    public String getSub() {
        return sub;
    }

    public AccessRequest setSub(String sub) {
        this.sub = sub;
        return this;
    }
}
