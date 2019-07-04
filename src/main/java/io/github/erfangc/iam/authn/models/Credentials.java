package io.github.erfangc.iam.authn.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Credentials {
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("id_token")
    private String idToken;
    @JsonProperty("token_type")
    private String tokenType;
    @JsonProperty("refresh_token")
    private String refreshToken;
    @JsonProperty("error_description")
    private String errorDescription;
    @JsonProperty("error")
    private String error;
    @JsonProperty("scope")
    private String scope;

    public String getAccessToken() {
        return accessToken;
    }

    public Credentials setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public String getIdToken() {
        return idToken;
    }

    public Credentials setIdToken(String idToken) {
        this.idToken = idToken;
        return this;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Credentials setTokenType(String tokenType) {
        this.tokenType = tokenType;
        return this;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Credentials setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public Credentials setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
        return this;
    }

    public String getError() {
        return error;
    }

    public Credentials setError(String error) {
        this.error = error;
        return this;
    }

    public String getScope() {
        return scope;
    }

    public Credentials setScope(String scope) {
        this.scope = scope;
        return this;
    }
}
