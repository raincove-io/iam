package io.github.erfangc.iam.authz.services;

import io.github.erfangc.iam.authz.models.*;
import io.github.erfangc.iam.mocks.roles.BindingProvider;
import io.lettuce.core.RedisClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import redis.embedded.RedisServer;

import java.util.List;

import static io.github.erfangc.iam.mocks.roles.RoleProvider.forId;
import static org.junit.Assert.assertEquals;

public class BindingsServiceTest {

    @Rule
    private ExpectedException expectedException = ExpectedException.none();
    private RedisServer redisServer;
    private BindingsService bindingsService;

    @Before
    public void setUp() throws Exception {
        redisServer = new RedisServer(6379);
        redisServer.start();
        RedisClient redisClient = RedisClient.create("localhost:6379");
        RolesService rolesService = new RolesService(redisClient);
        rolesService.createOrUpdateRole(new CreateOrUpdateRoleRequest().setRole(forId("users")));
        bindingsService = new BindingsService(redisClient);
    }

    @After
    public void tearDown() throws Exception {
        redisServer.stop();
    }

    @Test
    public void getBindings() {
        bindingsService.createOrUpdateBinding(new CreateOrUpdateBindingRequest().setBinding(BindingProvider.forId("joe-as-user")), "users");
        bindingsService.createOrUpdateBinding(new CreateOrUpdateBindingRequest().setBinding(BindingProvider.forId("john-as-user")), "users");
        final GetBindingsResponse resp = bindingsService.getBindings("users");
        final List<Binding> bindings = resp.getBindings();
        assertEquals(2, bindings.size());
    }

    @Test
    public void createOrUpdateBinding() {
        final CreateOrUpdateBindingResponse resp = bindingsService.createOrUpdateBinding(new CreateOrUpdateBindingRequest().setBinding(BindingProvider.forId("joe-as-user")), "users");
        assertEquals("Created", resp.getMessage());
        final GetBindingResponse resp2 = bindingsService.getBinding("users", "joe-as-user");
        assertEquals("user", resp2.getBinding().getPrincipalType());
        assertEquals("joe", resp2.getBinding().getPrincipalId());
        final CreateOrUpdateBindingRequest body = new CreateOrUpdateBindingRequest()
                .setBinding(BindingProvider.forId("joe-as-user").setPrincipalType("group"));
        CreateOrUpdateBindingResponse resp3 = bindingsService.createOrUpdateBinding(body, "users");
        assertEquals("Updated", resp3.getMessage());
        assertEquals("group", bindingsService.getBinding("users", "joe-as-user").getBinding().getPrincipalType());
    }

    @Test
    public void getBinding() {
        bindingsService.createOrUpdateBinding(new CreateOrUpdateBindingRequest().setBinding(BindingProvider.forId("joe-as-user")), "users");
        final GetBindingResponse resp = bindingsService.getBinding("users", "joe-as-user");
        assertEquals("joe-as-user", resp.getBinding().getId());
    }

    @Test
    public void deleteBinding() {
    }
}