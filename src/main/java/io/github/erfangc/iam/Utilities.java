package io.github.erfangc.iam;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

class Utilities {
    static final String STATE = "state";
    static final String CREDENTIALS = "credentials";
    static final String X_ORIGINAL_URI = "X-Original-URI";
    static final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules().
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
}
