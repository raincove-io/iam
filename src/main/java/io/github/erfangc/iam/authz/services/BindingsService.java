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
public class BindingsService {
    private static final Logger logger = LoggerFactory.getLogger(BindingsService.class);
    private StatefulRedisConnection<String, String> conn;

    public BindingsService(RedisClient redisClient) {
        conn = redisClient.connect();
    }

    public GetBindingsResponse getBindings(String roleId) {
        final RedisCommands<String, String> sync = conn.sync();
        final Set<String> bindingIds = sync.smembers(roleBindingsKey(roleId));
        List<Binding> bindings = new ArrayList<>();
        for (String bindingId : bindingIds) {
            final String json = sync.get(bindingKey(bindingId));
            try {
                final Binding binding = objectMapper.readValue(json, Binding.class);
                bindings.add(binding);
            } catch (IOException e) {
                logger.error("Cannot deserialize binding error={}", e.getMessage());
            }
        }
        return new GetBindingsResponse().setBindings(bindings);
    }

    /**
     * A {@link Binding} consists of a principalId and a roleId (in addition to the principalType). Bindings are usually
     * accessed in two patterns: all bindings for a role or all bindings across all roles for a principal
     * <p>
     * As such, we create a Set for each principal, containing the roleId(s) that the principal is bound. Each create/delete/modify operation on
     * {@link Binding} mutates this set (i.e. SADD, SREM)
     * <p>
     * Furthermore, we create another Set containing all bindingIds for the given roleId to support the query case of get all bindings for a role.
     */
    public CreateOrUpdateBindingResponse createOrUpdateBinding(CreateOrUpdateBindingRequest body, String roleId) {
        CreateOrUpdateBindingResponse ret = new CreateOrUpdateBindingResponse();
        final RedisCommands<String, String> sync = conn.sync();
        final Binding binding = body.getBinding();
        binding.setRoleId(roleId);
        final String id = binding.getId();
        if (id == null) {
            throw new ApiException()
                    .setHttpStatus(HttpStatus.BAD_REQUEST)
                    .setMessage("id is required");
        }
        final String pk = bindingKey(id);
        if (sync.exists(pk) == 1) {
            //
            // binding does not exist, this is not a create operation
            //
            try {
                final String json = sync.get(pk);
                Binding existing = objectMapper.readValue(json, Binding.class);
                //
                // figure out what is different between the existing and the to be updated binding
                //
                sync.multi();
                sync.set(pk, objectMapper.writeValueAsString(binding));
                sync.srem(subBindingsKey(existing.getPrincipalType(), existing.getPrincipalId()), existing.getId());
                sync.sadd(subBindingsKey(binding.getPrincipalType(), binding.getPrincipalId()), binding.getId());
                sync.exec();
            } catch (Exception e) {
                throw new ApiException(e)
                        .setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            ret.setMessage("Updated");
        } else {
            try {
                //
                // binding exists, this is an update operation
                //
                sync.multi();
                sync.set(pk, objectMapper.writeValueAsString(binding));
                sync.sadd(subBindingsKey(binding.getPrincipalType(), binding.getPrincipalId()), binding.getId());
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

    public GetBindingResponse getBinding(String roleId, String id) {
        final RedisCommands<String, String> sync = conn.sync();
        final String pk = bindingKey(id);
        final String json = sync.get(pk);
        if (json == null) {
            throw bindingNotFound(id);
        }
        try {
            return new GetBindingResponse().setBinding(objectMapper.readValue(json, Binding.class));
        } catch (Exception e) {
            throw new ApiException(e)
                    .setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public DeleteBindingResponse deleteBinding(String roleId, String id) {
        final RedisCommands<String, String> sync = conn.sync();
        String pk = bindingKey(id);
        if (sync.exists(pk) == 0) {
            throw bindingNotFound(id);
        }
        try {
            final String json = sync.get(pk);
            final Binding binding = objectMapper.readValue(json, Binding.class);
            sync.multi();
            sync.srem(subBindingsKey(binding.getPrincipalType(), binding.getPrincipalId()), binding.getId());
            sync.srem(roleBindingsKey(roleId), id);
            sync.del(pk);
            sync.exec();
            return new DeleteBindingResponse()
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
                .setMessage("Binding " + id + " not found");
    }
}
