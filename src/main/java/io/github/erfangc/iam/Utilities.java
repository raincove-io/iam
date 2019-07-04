package io.github.erfangc.iam;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Utilities {
    public static final String STATE = "state";
    public static final String CREDENTIALS = "credentials";
    public static final String SUB = "sub";
    public static final String X_AUTH_REQUEST_REDIRECT = "X-Auth-Request-Redirect";
    public static final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
}
