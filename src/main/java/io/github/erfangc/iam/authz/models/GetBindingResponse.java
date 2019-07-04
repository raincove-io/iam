package io.github.erfangc.iam.authz.models;


public class GetBindingResponse {

    private Binding binding;

    public Binding getBinding() {
        return this.binding;
    }

    public GetBindingResponse setBinding(Binding binding) {
        this.binding = binding;
        return this;
    }

}