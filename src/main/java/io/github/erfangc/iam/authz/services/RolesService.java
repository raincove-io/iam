package io.github.erfangc.iam.authz.services;

import io.github.erfangc.iam.authz.models.*;
import io.lettuce.core.RedisClient;
import org.springframework.stereotype.Service;

@Service
public class RolesService {
    private RedisClient redisClient;

    public RolesService(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    public GetAllRolesResponse getRoles() {
        return null;
    }

    public CreateOrUpdateRoleResponse createOrUpdateRole(CreateOrUpdateRoleRequest body) {
        return null;
    }

    public GetRoleResponse getRole(String id) {
        return null;
    }

    public DeleteRoleResponse deleteRole(String id) {
        return null;
    }
}
