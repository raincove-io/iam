package io.github.erfangc.iam.authz.services;

import io.github.erfangc.iam.authz.models.AuthorizeResponse;
import org.springframework.stereotype.Service;

@Service
public class AuthorizeService {
    public AuthorizeResponse authorizeRequest(String resource, String verb, String sub) {
        return null;
    }
}
