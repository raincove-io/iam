package io.github.erfangc.iam;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.TimeUnit;

/**
 * {@link JwtValidator} is a singleton helper that caches Jwks (Json Web Key Sets) from our OAuth 2.0 OpenID Connect
 * IdP and uses that information to validate JWT tokens. Users only have to use the {@link JwtValidator#decodeAndVerify(String)} method
 * once they've obtained the access token either through authorization header or session
 * <p>
 * The {@link JwtValidator#decodeAndVerify(String)} method not only verifies the signature but also produces a {@link DecodedJWT} instance
 * from which the caller can extract claims (such as the identity of the principal)
 */
public class JwtValidator {

    private String issuer = System.getenv("ISSUER");
    private String audience = System.getenv("AUDIENCE");
    private final JwkProvider jwkProvider;

    private static JwtValidator instance;

    private JwtValidator() {
        jwkProvider = new JwkProviderBuilder(issuer)
                .cached(5, 10, TimeUnit.HOURS)
                .build();
    }

    public static synchronized JwtValidator getInstance() {
        if (instance == null) {
            instance = new JwtValidator();
        }
        return instance;
    }

    public DecodedJWT decodeAndVerify(String token) {
        final DecodedJWT decodedJWT = JWT.decode(token);
        //
        // find the kid from jwkProvider, use that to construct the public key needed
        //
        final String kid = decodedJWT.getKeyId();
        try {
            final JWTVerifier jwtVerifier = jwtVerifierForKid(kid);
            return jwtVerifier.verify(decodedJWT);
        } catch (JwkException | JWTVerificationException e) {
            throw new UnauthenticatedException(e);
        }
    }

    private JWTVerifier jwtVerifierForKid(String kid) throws JwkException {
        final Jwk jwk = jwkProvider.get(kid);
        final RSAPublicKey publicKey = (RSAPublicKey) jwk.getPublicKey();
        return JWT.require(Algorithm.RSA256(publicKey, null)).withIssuer(issuer).withAudience(audience).acceptLeeway(0).build();
    }
}
