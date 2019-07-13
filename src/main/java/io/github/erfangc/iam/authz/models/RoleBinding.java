package io.github.erfangc.iam.authz.models;


public class RoleBinding {

    private String id;
    private String principalId;
    private String roleId;
    private String principalType;

    public String getId() {
        return this.id;
    }

    public RoleBinding setId(String id) {
        this.id = id;
        return this;
    }

    public String getPrincipalId() {
        return this.principalId;
    }

    public RoleBinding setPrincipalId(String principalId) {
        this.principalId = principalId;
        return this;
    }

    public String getRoleId() {
        return this.roleId;
    }

    public RoleBinding setRoleId(String roleId) {
        this.roleId = roleId;
        return this;
    }

    public String getPrincipalType() {
        return this.principalType;
    }

    public RoleBinding setPrincipalType(String principalType) {
        this.principalType = principalType;
        return this;
    }

}