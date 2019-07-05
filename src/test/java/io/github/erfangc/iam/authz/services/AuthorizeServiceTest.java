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
        AccessRequest accessRequest = new AccessRequest().setResource("/inventories/product1").setAction("GET").setSub("joe");
        assertTrue(authorizeService.authorizeRequest(accessRequest).getAllowed());
    }

    @Test
    public void test2() {
        AccessRequest accessRequest = new AccessRequest().setSub("joe").setAction("PUT").setResource("/inventories/product1");
        assertFalse(authorizeService.authorizeRequest(accessRequest).getAllowed());
    }

    @Test
    public void test3() {
        AccessRequest accessRequest = new AccessRequest().setResource("/inventories/product1").setAction("PUT").setSub("jack");
        assertTrue(authorizeService.authorizeRequest(accessRequest).getAllowed());
    }

    @Test
    public void test4() {
        AccessRequest accessRequest = new AccessRequest()
                .setResource("/inventories/product1")
                .setAction("GET")
                .setSub("noone");
        assertFalse(authorizeService.authorizeRequest(accessRequest).getAllowed());
    }

    @Test
    public void test5() {
        AccessRequest accessRequest = new AccessRequest()
                .setResource("/inventories/third-party-product1")
                .setAction("PUT")
                .setSub("joe");
        assertTrue(authorizeService.authorizeRequest(accessRequest).getAllowed());
        bindingsService.deleteBinding("contractors", "joe-as-contractor");
        AccessRequest accessRequest2 = new AccessRequest()
                .setResource("/inventories/third-party-product1")
                .setAction("PUT")
                .setSub("joe");
        assertFalse(authorizeService.authorizeRequest(accessRequest2).getAllowed());
    }

    @Test
    public void test6() {
        AccessRequest accessRequest = new AccessRequest()
                .setResource("/inventories/third-party-product1")
                .setAction("PUT")
                .setSub("joe");
        assertTrue(authorizeService.authorizeRequest(accessRequest).getAllowed());
        rolesService.deleteRole("contractors");
        assertFalse(authorizeService.authorizeRequest(accessRequest).getAllowed());
    }

    @Test
    public void test7() {
        AccessRequest accessRequest1 = new AccessRequest()
                .setResource("/esoteric-resource/foobar")
                .setAction("GET")
                .setSub("joe");
        assertFalse(authorizeService.authorizeRequest(accessRequest1).getAllowed());
        AccessRequest accessRequest2 = new AccessRequest()
                .setResource("/esoteric-resource/foobar")
                .setAction("GET")
                .setSub("john");
        assertFalse(authorizeService.authorizeRequest(accessRequest2).getAllowed());
        AccessRequest accessRequest3 = new AccessRequest()
                .setResource("/esoteric-resource/foobar")
                .setAction("GET")
                .setSub("noone");
        assertFalse(authorizeService.authorizeRequest(accessRequest3).getAllowed());
        AccessRequest accessRequest4 = new AccessRequest()
                .setResource("/esoteric-resource/foobar")
                .setAction("GET")
                .setSub("jack");
        assertTrue(authorizeService.authorizeRequest(accessRequest4).getAllowed());
    }

    @Test
    public void test8() {
        AccessRequest accessRequest1 = new AccessRequest()
                .setResource("/esoteric-resource/foobar")
                .setAction("GET")
                .setSub("joe");
        assertFalse(authorizeService.authorizeRequest(accessRequest1).getAllowed());
        final CreateOrUpdateBindingRequest body = new CreateOrUpdateBindingRequest()
                .setBinding(
                        new Binding()
                                .setPrincipalType("user")
                                .setId("joe-as-admin")
                                .setPrincipalId("joe")
                );
        bindingsService.createOrUpdateBinding(body, "admins");
        AccessRequest accessRequest2 = new AccessRequest()
                .setResource("/esoteric-resource/foobar")
                .setAction("GET")
                .setSub("joe");
        assertTrue(authorizeService.authorizeRequest(accessRequest2).getAllowed());
    }
}