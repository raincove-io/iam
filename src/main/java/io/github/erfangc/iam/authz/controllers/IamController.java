package io.github.erfangc.iam.authz.controllers;

import io.github.erfangc.iam.authz.models.*;
import io.github.erfangc.iam.authz.services.RoleBindingsService;
import io.github.erfangc.iam.authz.services.RolesService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/iam/api/v1")
public class IamController {

    private RolesService rolesService;
    private RoleBindingsService roleBindingsService;

    public IamController(RolesService rolesService, RoleBindingsService roleBindingsService) {
        this.rolesService = rolesService;
        this.roleBindingsService = roleBindingsService;
    }

    @RequestMapping(method = GET, path = "/roles")
    public GetAllRolesResponse getRoles() {
        return rolesService.getRoles();
    }

    @RequestMapping(method = POST, path = "/roles")
    public CreateOrUpdateRoleResponse createOrUpdateRole(@RequestBody CreateOrUpdateRoleRequest body) {
        return rolesService.createOrUpdateRole(body);
    }

    @RequestMapping(method = GET, path = "/roles/{id}")
    public GetRoleResponse getRole(@PathVariable String id) {
        return rolesService.getRole(id);
    }

    @RequestMapping(method = DELETE, path = "/roles/{id}")
    public DeleteRoleResponse deleteRole(@PathVariable String id) {
        return rolesService.deleteRole(id);
    }

    @RequestMapping(method = GET, path = "/roles/{roleId}/bindings")
    public GetRoleBindingsResponse getRoleBindings(@PathVariable String roleId) {
        return roleBindingsService.getRoleBindings(roleId);
    }

    @RequestMapping(method = POST, path = "/role-bindings")
    public CreateOrUpdateRoleBindingResponse createOrUpdateBinding(@RequestBody CreateOrUpdateRoleBindingRequest body) {
        return roleBindingsService.createOrUpdateRoleBinding(body);
    }

    @RequestMapping(method = GET, path = "/role-bindings/{id}")
    public GetRoleBindingResponse getRoleBinding(@PathVariable String id) {
        return roleBindingsService.getRoleBinding(id);
    }

    @RequestMapping(method = DELETE, path = "/role-bindings/{id}")
    public DeleteRoleBindingResponse deleteRoleBinding(@PathVariable String id) {
        return roleBindingsService.deleteRoleBinding(id);
    }

}