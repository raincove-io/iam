package io.github.erfangc.iam.authz.services;

import io.github.erfangc.iam.authz.models.*;
import io.github.erfangc.iam.mocks.roles.RoleProvider;
import io.lettuce.core.RedisClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import redis.embedded.RedisServer;

import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RolesServiceTest {

    @Rule
    private ExpectedException expectedException = ExpectedException.none();
    private RedisServer redisServer;
    private RolesService rolesService;
    private BindingsService bindingsService;

    @Before
    public void setUp() throws Exception {
        redisServer = new RedisServer(6379);
        redisServer.start();
        RedisClient redisClient = RedisClient.create("localhost:6379");
        rolesService = new RolesService(redisClient);
        bindingsService = new BindingsService(redisClient);
    }

    @After
    public void tearDown() throws Exception {
        redisServer.stop();
    }

    @Test
    public void getRoles() {
        final Role admin = RoleProvider.forId("admin");
        final Role workers = RoleProvider.forId("workers");
        final Role users = RoleProvider.forId("users");
        rolesService.createOrUpdateRole(new CreateOrUpdateRoleRequest().setRole(admin));
        rolesService.createOrUpdateRole(new CreateOrUpdateRoleRequest().setRole(workers));
        rolesService.createOrUpdateRole(new CreateOrUpdateRoleRequest().setRole(users));
        final GetAllRolesResponse resp = rolesService.getRoles();
        assertEquals(3, resp.getRoles().size());
    }

    @Test
    public void createOrUpdateRole() {
        final Role admin = RoleProvider.forId("admin");
        final CreateOrUpdateRoleResponse resp = rolesService.createOrUpdateRole(new CreateOrUpdateRoleRequest().setRole(admin));
        assertEquals("Created", resp.getMessage());
        admin.setPolicies(emptyList());
        final CreateOrUpdateRoleResponse resp2 = rolesService.createOrUpdateRole(new CreateOrUpdateRoleRequest().setRole(admin));
        assertEquals("Updated", resp2.getMessage());
        final GetRoleResponse resp3 = rolesService.getRole(admin.getId());
        assertTrue(resp3.getRole().getPolicies().isEmpty());
    }

    @Test
    public void getRole() {
        final Role users = RoleProvider.forId("users");
        rolesService.createOrUpdateRole(new CreateOrUpdateRoleRequest().setRole(users));
        final GetRoleResponse resp = rolesService.getRole(users.getId());
        assertEquals(users.getId(), resp.getRole().getId());
    }

    @Test
    public void deleteRole() {
        final Role users = RoleProvider.forId("users");
        rolesService.createOrUpdateRole(new CreateOrUpdateRoleRequest().setRole(users));
        final String bindingId = UUID.randomUUID().toString();
        final Binding binding = new Binding()
                .setId(bindingId)
                .setPrincipalId("joe")
                .setPrincipalType("user");
        bindingsService.createOrUpdateBinding(new CreateOrUpdateBindingRequest().setBinding(binding), users.getId());
        assertEquals(1, bindingsService.getBindings(users.getId()).getBindings().size());
        //
        // now delete the role
        //
        final DeleteRoleResponse resp = rolesService.deleteRole(users.getId());
        assertEquals("Deleted", resp.getMessage());

        //
        // deleting a role should delete all bindings
        //
        final GetBindingsResponse bindings = bindingsService.getBindings(users.getId());
        assertTrue(bindings.getBindings().isEmpty());

        //
        // trying to retrieve an non-existent role should result in error
        //
        expectedException.expect(RoleNotFoundException.class);
        rolesService.getRole(users.getId());
    }
}