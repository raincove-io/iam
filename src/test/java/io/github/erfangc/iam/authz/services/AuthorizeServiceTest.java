package io.github.erfangc.iam.authz.services;

import io.github.erfangc.iam.authz.models.Binding;
import io.github.erfangc.iam.authz.models.CreateOrUpdateBindingRequest;
import io.github.erfangc.iam.authz.models.CreateOrUpdateRoleRequest;
import io.github.erfangc.iam.mocks.roles.BindingProvider;
import io.github.erfangc.iam.mocks.roles.RoleProvider;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.embedded.RedisServer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AuthorizeServiceTest {

    private RedisServer redisServer;
    private RolesService rolesService;
    private BindingsService bindingsService;
    private RedisClient redisClient;
    private AuthorizeService authorizeService;

    @Before
    public void setUp() throws Exception {
        redisServer = new RedisServer(6379);
        redisServer.start();
        redisClient = RedisClient.create("redis://localhost:6379");
        rolesService = new RolesService(redisClient);
        bindingsService = new BindingsService(redisClient);
        authorizeService = new AuthorizeService(redisClient);
        //
        // setup roles and bindings for tests
        //
        rolesService.createOrUpdateRole(new CreateOrUpdateRoleRequest().setRole(RoleProvider.forId("admins")));
        rolesService.createOrUpdateRole(new CreateOrUpdateRoleRequest().setRole(RoleProvider.forId("users")));
        rolesService.createOrUpdateRole(new CreateOrUpdateRoleRequest().setRole(RoleProvider.forId("contractors")));
        bindingsService.createOrUpdateBinding(new CreateOrUpdateBindingRequest().setBinding(BindingProvider.forId("joe-as-user")), "users");
        bindingsService.createOrUpdateBinding(new CreateOrUpdateBindingRequest().setBinding(BindingProvider.forId("joe-as-contractor")), "contractors");
        bindingsService.createOrUpdateBinding(new CreateOrUpdateBindingRequest().setBinding(BindingProvider.forId("john-as-user")), "users");
        bindingsService.createOrUpdateBinding(new CreateOrUpdateBindingRequest().setBinding(BindingProvider.forId("jack-as-admin")), "admins");
    }


    private void deleteAllKeys() {
        final RedisCommands<String, String> sync = redisClient.connect().sync();
        for (String key : sync.keys("*")) {
            sync.del(key);
        }
    }

    @After
    public void tearDown() {
        deleteAllKeys();
        redisServer.stop();
    }

    @Test
    public void test1() {
        assertTrue(authorizeService.authorizeRequest("/inventories/product1", "GET", "joe").getAllowed());
    }

    @Test
    public void test2() {
        assertFalse(authorizeService.authorizeRequest("/inventories/product1", "PUT", "joe").getAllowed());
    }

    @Test
    public void test3() {
        assertTrue(authorizeService.authorizeRequest("/inventories/product1", "PUT", "jack").getAllowed());
    }

    @Test
    public void test4() {
        assertFalse(authorizeService.authorizeRequest("/inventories/product1", "GET", "noone").getAllowed());
    }

    @Test
    public void test5() {
        assertTrue(authorizeService.authorizeRequest("/inventories/third-party-product1", "PUT", "joe").getAllowed());
        bindingsService.deleteBinding("contractors", "joe-as-contractor");
        assertFalse(authorizeService.authorizeRequest("/inventories/third-party-product1", "PUT", "joe").getAllowed());
    }

    @Test
    public void test6() {
        assertTrue(authorizeService.authorizeRequest("/inventories/third-party-product1", "PUT", "joe").getAllowed());
        rolesService.deleteRole("contractors");
        assertFalse(authorizeService.authorizeRequest("/inventories/third-party-product1", "PUT", "joe").getAllowed());
    }

    @Test
    public void test7() {
        assertFalse(authorizeService.authorizeRequest("/esoteric-resource/foobar", "GET", "joe").getAllowed());
        assertFalse(authorizeService.authorizeRequest("/esoteric-resource/foobar", "GET", "john").getAllowed());
        assertFalse(authorizeService.authorizeRequest("/esoteric-resource/foobar", "GET", "noone").getAllowed());
        assertTrue(authorizeService.authorizeRequest("/esoteric-resource/foobar", "GET", "jack").getAllowed());
    }

    @Test
    public void test8() {
        assertFalse(authorizeService.authorizeRequest("/esoteric-resource/foobar", "GET", "joe").getAllowed());
        final CreateOrUpdateBindingRequest body = new CreateOrUpdateBindingRequest()
                .setBinding(
                        new Binding()
                                .setPrincipalType("user")
                                .setId("joe-as-admin")
                                .setPrincipalId("joe")
                );
        bindingsService.createOrUpdateBinding(body, "admins");
        assertTrue(authorizeService.authorizeRequest("/esoteric-resource/foobar", "GET", "joe").getAllowed());
    }
}