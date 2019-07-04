package io.github.erfangc.iam.authz.services;

import io.github.erfangc.iam.authz.models.*;
import io.lettuce.core.RedisClient;
import org.springframework.stereotype.Service;

@Service
public class BindingsService {
    private final RedisClient redisClient;

    public BindingsService(RedisClient redisClient) {

        this.redisClient = redisClient;
    }

    public GetBindingsResponse getBindings(String roleId) {
        return null;
    }

    public CreateOrUpdateBindingResponse createOrUpdateBinding(CreateOrUpdateBindingRequest body, String roleId) {
        return null;
    }

    public GetBindingResponse getBinding(String roleId, String id) {
        return null;
    }

    public DeleteBindingResponse deleteBinding(String roleId, String id) {
        return null;
    }
}
