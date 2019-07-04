package io.github.erfangc.iam.authz.models;

import java.util.List;

public class Role {

    private String id;
    private List<Policy> policies;

    public String getId() {
        return this.id;
    }

    public Role setId(String id) {
        this.id = id;
        return this;
    }

    public List<Policy> getPolicies() {
        return this.policies;
    }

    public Role setPolicies(List<Policy> policies) {
        this.policies = policies;
        return this;
    }

}