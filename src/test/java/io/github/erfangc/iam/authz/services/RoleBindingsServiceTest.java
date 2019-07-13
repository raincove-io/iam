package io.github.erfangc.iam.authz.services;

import io.github.erfangc.iam.authz.models.*;
import io.github.erfangc.iam.mocks.roles.RoleBindingProvider;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import redis.embedded.RedisServer;

import java.util.List;

import static io.github.erfangc.iam.mocks.roles.RoleProvider.forId;
import static org.junit.Assert.assertEquals;

public class RoleBindingsServiceTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private RedisServer redisServer;
    private RoleBindingsService roleBindingsService;
    private RedisClient redisClient;

    @Before
    public void setUp() throws Exception {
        redisServer = new RedisServer(6379);
        redisServer.start();
        redisClient = RedisClient.create("redis://localhost:6379");
        RolesService rolesService = new RolesService(redisClient);
        rolesService.createOrUpdateRole(new CreateOrUpdateRoleRequest().setRole(forId("users")));
        roleBindingsService = new RoleBindingsService(redisClient);
    }

    @After
    public void tearDown() {
        deleteAllKeys();
        redisServer.stop();
    }

    private void deleteAllKeys() {
        final RedisCommands<String, String> sync = redisClient.connect().sync();
        for (String key : sync.keys("*")) {
            sync.del(key);
        }
    }

    @Test
    public void getBindings() {
        roleBindingsService.createOrUpdateRoleBinding(new CreateOrUpdateRoleBindingRequest().setRoleBinding(RoleBindingProvider.forId("joe-as-user")));
        roleBindingsService.createOrUpdateRoleBinding(new CreateOrUpdateRoleBindingRequest().setRoleBinding(RoleBindingProvider.forId("john-as-user")));
        final GetRoleBindingsResponse resp = roleBindingsService.getRoleBindings("users");
        final List<RoleBinding> roleBindings = resp.getRoleBindings();
        assertEquals(2, roleBindings.size());
    }

    @Test
    public void createOrUpdateBinding() {
        final CreateOrUpdateRoleBindingResponse resp = roleBindingsService.createOrUpdateRoleBinding(new CreateOrUpdateRoleBindingRequest().setRoleBinding(RoleBindingProvider.forId("joe-as-user")));
        assertEquals("Created", resp.getMessage());
        final GetRoleBindingResponse resp2 = roleBindingsService.getRoleBinding("joe-as-user");
        assertEquals("user", resp2.getRoleBinding().getPrincipalType());
        assertEquals("joe", resp2.getRoleBinding().getPrincipalId());
        final CreateOrUpdateRoleBindingRequest body = new CreateOrUpdateRoleBindingRequest()
                .setRoleBinding(RoleBindingProvider.forId("joe-as-user").setPrincipalType("group"));
        CreateOrUpdateRoleBindingResponse resp3 = roleBindingsService.createOrUpdateRoleBinding(body);
        assertEquals("Updated", resp3.getMessage());
        assertEquals("group", roleBindingsService.getRoleBinding("joe-as-user").getRoleBinding().getPrincipalType());
    }

    @Test
    public void getBinding() {
        roleBindingsService.createOrUpdateRoleBinding(new CreateOrUpdateRoleBindingRequest().setRoleBinding(RoleBindingProvider.forId("joe-as-user")));
        final GetRoleBindingResponse resp = roleBindingsService.getRoleBinding("joe-as-user");
        assertEquals("joe-as-user", resp.getRoleBinding().getId());
    }

    @Test
    public void deleteBinding() {
    }
}