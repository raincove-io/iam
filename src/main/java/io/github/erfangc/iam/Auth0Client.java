package io.github.erfangc.iam;

import feign.Body;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface Auth0Client {
    @RequestLine("POST /oauth/token")
    @Headers({"Content-Type: application/x-www-form-urlencoded"})
    @Body("grant_type={grant_type}&client_id={client_id}&client_secret={client_secret}&redirect_uri={redirect_uri}&code={code}")
    Credentials exchangeCode(
            @Param("grant_type") String grantType,
            @Param("client_id") String clientId,
            @Param("client_secret") String clientSecret,
            @Param("code") String code,
            @Param("redirect_uri") String redirectUri
    );
}
