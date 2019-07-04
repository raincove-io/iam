package io.github.erfangc.iam.authz.controllers;

import io.github.erfangc.iam.authz.models.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/iam/api/v1")
public class IamController {

    @RequestMapping(method = GET, path = "/roles")
    public GetAllRolesResponse getRoles() {
        throw new UnsupportedOperationException();
    }

    @RequestMapping(method = POST, path = "/roles")
    public CreateOrUpdateRoleResponse createOrUpdateRole(@RequestBody CreateOrUpdateRoleRequest body) {
        throw new UnsupportedOperationException();
    }

    @RequestMapping(method = GET, path = "/roles/{id}")
    public GetRoleResponse getRole(@PathVariable String id) {
        throw new UnsupportedOperationException();
    }

    @RequestMapping(method = DELETE, path = "/roles/{id}")
    public DeleteRoleResponse deleteRole(@PathVariable String id) {
        throw new UnsupportedOperationException();
    }

    @RequestMapping(method = GET, path = "/roles/{roleId}/bindings")
    public GetBindingsResponse getBindings(@PathVariable String roleId) {
        throw new UnsupportedOperationException();
    }

    @RequestMapping(method = POST, path = "/roles/{roleId}/bindings")
    public CreateOrUpdateBindingResponse createOrUpdateBinding(@RequestBody CreateOrUpdateBindingRequest body, @PathVariable String roleId) {
        throw new UnsupportedOperationException();
    }

    @RequestMapping(method = GET, path = "/roles/{roleId}/bindings/{id}")
    public GetBindingResponse getBinding(@PathVariable String roleId, @PathVariable String id) {
        throw new UnsupportedOperationException();
    }

    @RequestMapping(method = DELETE, path = "/roles/{roleId}/bindings/{id}")
    public DeleteBindingResponse deleteBinding(@PathVariable String roleId, @PathVariable String id) {
        throw new UnsupportedOperationException();
    }

}