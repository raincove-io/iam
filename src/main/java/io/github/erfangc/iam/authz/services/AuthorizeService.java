package io.github.erfangc.iam.authz.services;

import io.github.erfangc.iam.ApiException;
import io.github.erfangc.iam.authz.models.AuthorizeResponse;
import io.github.erfangc.iam.authz.models.Binding;
import io.github.erfangc.iam.authz.models.Policy;
import io.github.erfangc.iam.authz.models.Role;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static io.github.erfangc.iam.Utilities.objectMapper;
import static io.github.erfangc.iam.authz.services.Namespaces.bindingKey;
import static io.github.erfangc.iam.authz.services.Namespaces.roleKey;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

@Service
public class AuthorizeService {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizeService.class);
    private StatefulRedisConnection<String, String> conn;
    private List<String> rootUsers;

    public AuthorizeService(RedisClient redisClient) {
        conn = redisClient.connect();
        final String rootUsers = System.getenv("ROOT_USERS");
        this.rootUsers = asList((rootUsers == null ? "" : rootUsers).split(","));
        logger.info("{} has started with root users={}", AuthorizeService.class.getSimpleName(), this.rootUsers);
    }

    private static String toRegex(String input) {
        return Stream
                .of(input.split("/"))
                .map(token -> {
                    if (token.equals("*")) {
                        return ".*";
                    } else {
                        return token;
                    }
                })
                .collect(joining("\\/"));
    }

    public AuthorizeResponse authorizeRequest(AccessRequest accessRequest) {
        try {
            //
            // short circuit everything if a root user login. This is necessary to bootstrap the RBAC system
            //
            if (rootUsers.contains(accessRequest.getSub())) {
                logger.info("Authorized root user access to sub={} resource={} action={}", accessRequest.getSub(), accessRequest.getResource(), accessRequest.getAction());
                return allowed();
            }
            final String sub = accessRequest.getSub();
            final RedisCommands<String, String> sync = conn.sync();
            //
            // users only for now, but add support for groups in the future
            //
            String subRoleMappingNS = "iam:bindings:subs:";
            final Set<String> bindingIds = sync.smembers(subRoleMappingNS + "user:" + sub);
            if (bindingIds.isEmpty()) {
                logger.info("Access is denied to sub={} resource={} action={}", accessRequest.getSub(), accessRequest.getResource(), accessRequest.getAction());
                return denied();
            } else {
                for (String bindingId : bindingIds) {
                    //
                    // find the role the binding refers to (this potentially can speed up)
                    //
                    Binding binding = objectMapper.readValue(sync.get(bindingKey(bindingId)), Binding.class);
                    final String roleId = binding.getRoleId();
                    final String roleKey = roleKey(roleId);
                    if (sync.exists(roleKey) == 1) {
                        Role role = objectMapper.readValue(sync.get(roleKey), Role.class);
                        if (makeAccessDecision(role, accessRequest)) {
                            return allowed();
                        }
                    } else {
                        logger.warn("Role cannot be found bindingId={} roleId={}", bindingId, roleId);
                    }
                }
                logger.info("Access is denied to sub={} resource={} action={}", accessRequest.getSub(), accessRequest.getResource(), accessRequest.getAction());
                return denied();
            }
        } catch (IOException e) {
            throw new ApiException(e).setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean makeAccessDecision(Role role, AccessRequest accessRequest) {
        final List<Policy> policies = role.getPolicies();
        final String action = accessRequest.getAction();
        final String resource = accessRequest.getResource();
        final String sub = accessRequest.getSub();
        for (Policy policy : policies) {
            boolean resourceMatch = resourceMatch(policy.getResource(), resource);
            boolean actionMatch = actionMatch(policy.getActions(), action);
            if (resourceMatch && actionMatch) {
                logger.info(
                        "Authorized access to sub={} resource={} action={} effectiveRoleId={} policyResource={} policyActions={}",
                        sub,
                        resource,
                        action,
                        role.getId(),
                        policy.getResource(),
                        policy.getActions()
                );
                return true;
            }
        }
        return false;
    }

    private boolean actionMatch(List<String> actions, String action) {
        return actions.contains("*") || actions.contains(action);
    }

    private boolean resourceMatch(String resourceExpr, String resource) {
        final String regex = toRegex(resourceExpr);
        return resource.matches(regex);
    }

    private AuthorizeResponse allowed() {
        return new AuthorizeResponse()
                .setAllowed(true)
                .setMessage("Access granted")
                .setTimestamp(Instant.now().toString());
    }

    private AuthorizeResponse denied() {
        return new AuthorizeResponse()
                .setAllowed(false)
                .setMessage("Access denied")
                .setTimestamp(Instant.now().toString());
    }


}
