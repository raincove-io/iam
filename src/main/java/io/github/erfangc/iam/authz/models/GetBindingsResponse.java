package io.github.erfangc.iam.authz.models;

import java.util.List;

public class GetBindingsResponse {

    private List<Binding> bindings;

    public List<Binding> getBindings() {
        return this.bindings;
    }

    public GetBindingsResponse setBindings(List<Binding> bindings) {
        this.bindings = bindings;
        return this;
    }

}