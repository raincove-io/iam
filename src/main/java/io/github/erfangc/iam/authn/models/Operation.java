package io.github.erfangc.iam.authn.models;

import java.util.Set;

public class Operation {
    private String resource;
    private Set<String> verbs;

    public String getResource() {
        return resource;
    }

    public Operation setResource(String resource) {
        this.resource = resource;
        return this;
    }

    public Set<String> getVerbs() {
        return verbs;
    }

    public Operation setVerbs(Set<String> verbs) {
        this.verbs = verbs;
        return this;
    }
}
