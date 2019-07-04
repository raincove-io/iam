package io.github.erfangc.iam.authz.models;


public class GetRoleResponse {

    private Role role;

    public Role getRole() {
        return this.role;
    }

    public GetRoleResponse setRole(Role role) {
        this.role = role;
        return this;
    }

}