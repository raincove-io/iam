package io.github.erfangc.iam.authz.models;

import java.util.List;

public class Policy {

    private String resource;
    private List<String> actions;

    public String getResource() {
        return this.resource;
    }

    public Policy setResource(String resource) {
        this.resource = resource;
        return this;
    }

    public List<String> getActions() {
        return this.actions;
    }

    public Policy setActions(List<String> actions) {
        this.actions = actions;
        return this;
    }

}