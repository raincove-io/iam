package io.github.erfangc.iam.authz.controllers;

import io.github.erfangc.iam.authz.models.*;
import io.github.erfangc.iam.authz.services.BindingsService;
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
    private BindingsService bindingsService;

    public IamController(RolesService rolesService, BindingsService bindingsService) {
        this.rolesService = rolesService;
        this.bindingsService = bindingsService;
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
    public GetBindingsResponse getBindings(@PathVariable String roleId) {
        return bindingsService.getBindings(roleId);
    }

    @RequestMapping(method = POST, path = "/roles/{roleId}/bindings")
    public CreateOrUpdateBindingResponse createOrUpdateBinding(@RequestBody CreateOrUpdateBindingRequest body, @PathVariable String roleId) {
        return bindingsService.createOrUpdateBinding(body, roleId);
    }

    @RequestMapping(method = GET, path = "/roles/{roleId}/bindings/{id}")
    public GetBindingResponse getBinding(@PathVariable String roleId, @PathVariable String id) {
        return bindingsService.getBinding(roleId, id);
    }

    @RequestMapping(method = DELETE, path = "/roles/{roleId}/bindings/{id}")
    public DeleteBindingResponse deleteBinding(@PathVariable String roleId, @PathVariable String id) {
        return bindingsService.deleteBinding(roleId, id);
    }

}