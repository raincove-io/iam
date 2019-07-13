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
import static io.github.erfangc.iam.authz.services.Namespaces.*;

@Service
public class RoleBindingsService {
    private static final Logger logger = LoggerFactory.getLogger(RoleBindingsService.class);
    private StatefulRedisConnection<String, String> conn;

    public RoleBindingsService(RedisClient redisClient) {
        conn = redisClient.connect();
    }

    public GetRoleBindingsResponse getRoleBindings(String roleId) {
        final RedisCommands<String, String> sync = conn.sync();
        final Set<String> bindingIds = sync.smembers(roleBindingsKey(roleId));
        List<RoleBinding> roleBindings = new ArrayList<>();
        for (String bindingId : bindingIds) {
            final String json = sync.get(roleBindingKey(bindingId));
            try {
                final RoleBinding roleBinding = objectMapper.readValue(json, RoleBinding.class);
                roleBindings.add(roleBinding);
            } catch (IOException e) {
                logger.error("Cannot deserialize binding error={}", e.getMessage());
            }
        }
        return new GetRoleBindingsResponse().setRoleBindings(roleBindings);
    }

    /**
     * A {@link RoleBinding} consists of a principalId and a roleId (in addition to the principalType). Bindings are usually
     * accessed in two patterns: all role-bindings for a role or all role-bindings across all roles for a principal
     * <p>
     * As such, we create a Set for each principal, containing the roleId(s) that the principal is bound. Each create/delete/modify operation on
     * {@link RoleBinding} mutates this set (i.e. SADD, SREM)
     * <p>
     * Furthermore, we create another Set containing all bindingIds for the given roleId to support the query case of get all role-bindings for a role.
     */
    public CreateOrUpdateRoleBindingResponse createOrUpdateRoleBinding(CreateOrUpdateRoleBindingRequest body) {
        CreateOrUpdateRoleBindingResponse ret = new CreateOrUpdateRoleBindingResponse();
        final RedisCommands<String, String> sync = conn.sync();
        final RoleBinding roleBinding = body.getRoleBinding();
        final String roleId = roleBinding.getRoleId();
        if (roleId == null || roleId.isEmpty()) {
            throw new ApiException().setHttpStatus(HttpStatus.BAD_REQUEST).setMessage("roleId must not be empty");
        }
        roleBinding.setRoleId(roleId);
        final String id = roleBinding.getId();
        final String pk = roleBindingKey(id);
        if (sync.exists(pk) == 1) {
            //
            // roleBinding does not exist, this is not a create operation
            //
            try {
                final String json = sync.get(pk);
                RoleBinding existing = objectMapper.readValue(json, RoleBinding.class);
                //
                // figure out what is different between the existing and the to be updated roleBinding
                //
                sync.multi();
                sync.set(pk, objectMapper.writeValueAsString(roleBinding));
                sync.srem(subBindingsKey(existing.getPrincipalType(), existing.getPrincipalId()), existing.getId());
                sync.sadd(subBindingsKey(roleBinding.getPrincipalType(), roleBinding.getPrincipalId()), roleBinding.getId());
                sync.exec();
            } catch (Exception e) {
                throw new ApiException(e)
                        .setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            ret.setMessage("Updated");
        } else {
            try {
                //
                // roleBinding exists, this is an update operation
                //
                sync.multi();
                sync.set(pk, objectMapper.writeValueAsString(roleBinding));
                sync.sadd(subBindingsKey(roleBinding.getPrincipalType(), roleBinding.getPrincipalId()), roleBinding.getId());
                sync.sadd(roleBindingsKey(roleId), id);
                sync.exec();
            } catch (Exception e) {
                throw new ApiException(e)
                        .setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            ret.setMessage("Created");
        }
        return ret;
    }

    public GetRoleBindingResponse getRoleBinding(String id) {
        final RedisCommands<String, String> sync = conn.sync();
        final String pk = roleBindingKey(id);
        final String json = sync.get(pk);
        if (json == null) {
            throw bindingNotFound(id);
        }
        try {
            return new GetRoleBindingResponse().setRoleBinding(objectMapper.readValue(json, RoleBinding.class));
        } catch (Exception e) {
            throw new ApiException(e)
                    .setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public DeleteRoleBindingResponse deleteRoleBinding(String id) {
        final RedisCommands<String, String> sync = conn.sync();
        String pk = roleBindingKey(id);
        if (sync.exists(pk) == 0) {
            throw bindingNotFound(id);
        }
        try {
            final String json = sync.get(pk);
            final RoleBinding roleBinding = objectMapper.readValue(json, RoleBinding.class);
            final String roleId = roleBinding.getRoleId();
            sync.multi();
            sync.srem(subBindingsKey(roleBinding.getPrincipalType(), roleBinding.getPrincipalId()), roleBinding.getId());
            sync.srem(roleBindingsKey(roleId), id);
            sync.del(pk);
            sync.exec();
            return new DeleteRoleBindingResponse()
                    .setTimestamp(Instant.now().toString())
                    .setMessage("Deleted");
        } catch (Exception e) {
            throw new ApiException(e)
                    .setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ApiException bindingNotFound(String id) {
        return new ApiException()
                .setHttpStatus(HttpStatus.NOT_FOUND)
                .setMessage("RoleBinding " + id + " not found");
    }
}
