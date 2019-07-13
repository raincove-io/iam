package io.github.erfangc.iam.authz.models;

import java.util.List;

public class GetRoleBindingsResponse {

    private List<RoleBinding> roleBindings;

    public List<RoleBinding> getRoleBindings() {
        return this.roleBindings;
    }

    public GetRoleBindingsResponse setRoleBindings(List<RoleBinding> roleBindings) {
        this.roleBindings = roleBindings;
        return this;
    }

}