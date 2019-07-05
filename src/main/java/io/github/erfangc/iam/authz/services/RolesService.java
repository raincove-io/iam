package io.github.erfangc.iam.authz.services;

import io.github.erfangc.iam.ApiException;
import io.github.erfangc.iam.authz.models.*;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.github.erfangc.iam.Utilities.objectMapper;
import static io.github.erfangc.iam.authz.services.Namespaces.ROLE_NS;
import static io.github.erfangc.iam.authz.services.Namespaces.roleKey;

@Service
public class RolesService {

    private static final Logger logger = LoggerFactory.getLogger(RolesService.class);
    private StatefulRedisConnection<String, String> conn;

    public RolesService(RedisClient redisClient) {
        conn = redisClient.connect();
    }

    public GetAllRolesResponse getRoles() {
        final RedisCommands<String, String> sync = conn.sync();
        final Set<String> roleIds = sync.smembers(ROLE_NS);
        List<Role> roles = new ArrayList<>();
        for (String roleId : roleIds) {
            final String json = sync.get(roleId);
            try {
                final Role role = objectMapper.readValue(json, Role.class);
                roles.add(role);
            } catch (IOException e) {
                logger.error("Cannot deserialize role id={}", roleId);
            }
        }
        return new GetAllRolesResponse().setRoles(roles);
    }

    public CreateOrUpdateRoleResponse createOrUpdateRole(CreateOrUpdateRoleRequest body) {
        CreateOrUpdateRoleResponse ret = new CreateOrUpdateRoleResponse();
        final RedisCommands<String, String> sync = conn.sync();
        final Role role = body.getRole();
        String id = role.getId();
        final String pk = roleKey(id);
        if (sync.exists(pk) == 0) {
            ret.setMessage("Created");
        } else {
            ret.setMessage("Updated");
        }
        try {
            final String json = objectMapper.writeValueAsString(role);
            sync.multi();
            sync.set(pk, json);
            sync.sadd(ROLE_NS, pk);
            sync.exec();
        } catch (IOException e) {
            throw new ApiException(e).setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        ret.setTimestamp(Instant.now().toString());
        return ret;
    }

    public GetRoleResponse getRole(String id) {
        final RedisCommands<String, String> sync = conn.sync();
        final String pk = roleKey(id);
        final String json = sync.get(pk);
        if (json == null) {
            throw roleNotFound(id);
        }
        try {
            final Role role = objectMapper.readValue(json, Role.class);
            return new GetRoleResponse().setRole(role);
        } catch (IOException e) {
            throw new ApiException(e).setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public DeleteRoleResponse deleteRole(String id) {
        DeleteRoleResponse ret = new DeleteRoleResponse();
        final RedisCommands<String, String> sync = conn.sync();
        String pk = roleKey(id);
        if (sync.exists(pk) == 0) {
            throw roleNotFound(id);
        }
        sync.del(pk);
        ret.setMessage("Deleted");
        return ret;
    }

    private ApiException roleNotFound(String id) {
        return new ApiException()
                .setHttpStatus(HttpStatus.NOT_FOUND)
                .setMessage("Role " + id + " not found");
    }
}
