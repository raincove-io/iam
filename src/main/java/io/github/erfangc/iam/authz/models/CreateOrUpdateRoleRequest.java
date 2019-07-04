package io.github.erfangc.iam.authz.models;


public class CreateOrUpdateRoleRequest {

    private Role role;

    public Role getRole() {
        return this.role;
    }

    public CreateOrUpdateRoleRequest setRole(Role role) {
        this.role = role;
        return this;
    }

}