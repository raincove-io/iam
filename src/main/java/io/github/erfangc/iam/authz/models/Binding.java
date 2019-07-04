package io.github.erfangc.iam.authz.models;


public class Binding {

    private String id;
    private String principalId;
    private String roleId;
    private String principalType;

    public String getId() {
        return this.id;
    }

    public Binding setId(String id) {
        this.id = id;
        return this;
    }

    public String getPrincipalId() {
        return this.principalId;
    }

    public Binding setPrincipalId(String principalId) {
        this.principalId = principalId;
        return this;
    }

    public String getRoleId() {
        return this.roleId;
    }

    public Binding setRoleId(String roleId) {
        this.roleId = roleId;
        return this;
    }

    public String getPrincipalType() {
        return this.principalType;
    }

    public Binding setPrincipalType(String principalType) {
        this.principalType = principalType;
        return this;
    }

}