package io.github.erfangc.iam.authn;

import com.auth0.jwt.interfaces.DecodedJWT;
import io.github.erfangc.iam.Utilities;
import io.github.erfangc.iam.authn.models.Credentials;
import io.github.erfangc.iam.authn.models.Operation;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static io.github.erfangc.iam.Utilities.*;
import static java.util.Collections.singleton;

/**
 * {@link IamAuthenticationFilter} invokes the session creation mechanism via spring-session-redis on requests that are not API calls. At the same time, this filter also
 * validates API calls who provides an access token through the Authorization header
 * <p>
 * Any call that provides such a token:
 * <pre>
 *     GET /path/to/any/thing HTTP/1.1
 *     Authorization: Bearer XXXXXX
 * </pre>
 * Will not require a session, and getSession() won't be called on the servlet request. Since invoking this creates a session if it does not exist.
 * <p>
 * We have to take care to ensure that this filter runs after the {@link org.springframework.session.web.http.SessionRepositoryFilter} of Spring Session
 * and that filter creates a wrapper {@link HttpServletRequest} object that we take advantage of here by calling it's <code>getSession()</code> method, which
 * creates a session if one does not exist
 */
@Component
public class IamAuthenticationFilter extends OncePerRequestFilter {

    /**
     * A list of {@link Operation} that does not require authentication
     */
    private final Set<Operation> noAuthOperations;
    private JwtValidator jwtValidator;

    public IamAuthenticationFilter(JwtValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
        noAuthOperations = new HashSet<>();
        noAuthOperations.add(
                new Operation()
                        .setResource("/")
                        .setVerbs(singleton("GET"))
        );
        noAuthOperations.add(
                new Operation()
                        .setResource("/iam/api/v1/callback")
                        .setVerbs(singleton("GET"))
        );
    }

    private boolean allowUnauthenticated(HttpServletRequest httpServletRequest) {
        final String resource = httpServletRequest.getRequestURI();
        final String verb = httpServletRequest.getMethod();
        for (Operation noAuthOperation : noAuthOperations) {
            if (noAuthOperation.getVerbs().contains(verb) && noAuthOperation.getResource().equals(resource)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        //
        // let the callback pass through without authentication
        //
        if (allowUnauthenticated(httpServletRequest)) {
            doFilter(httpServletRequest, httpServletResponse, filterChain);
        } else {
            final String authorization = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
            final boolean isApiCall = authorization != null && authorization.startsWith("Bearer ");
            DecodedJWT decodedJwt;
            String sub;
            if (isApiCall) {
                // handle JWT based authentication
                try {
                    decodedJwt = validateAccessToken(extractAccessToken(authorization));
                    logger.info("Authenticated API request for principal=" + decodedJwt.getSubject());
                    sub = decodedJwt.getSubject();
                } catch (UnauthenticatedException exception) {
                    // 401
                    httpServletResponse.setHeader("Content-Type", "application/json");
                    httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    httpServletResponse.getOutputStream().write(("{\"message\":\"Bearer token is not valid or is expired\", \"timestamp\": \"" + Instant.now().toString() + "\"}").getBytes());
                    logger.info("Anonymous access prohibited for API calls");
                    return;
                }
            } else {
                // handle session based authentication
                decodedJwt = validateSession(httpServletRequest, httpServletResponse);
                if (decodedJwt != null) {
                    logger.info("Authenticated request for principal=" + decodedJwt.getSubject());
                    sub = decodedJwt.getSubject();
                } else {
                    logger.info("Unable to authenticate web request");
                    return;
                }
            }
            httpServletRequest.setAttribute(SUB, sub);
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        }
    }

    private String extractAccessToken(String authorization) {
        return authorization.replaceFirst("^Bearer ", authorization);
    }

    private DecodedJWT validateSession(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws UnauthenticatedException {
        //
        // create or get a session based on cookie value
        //
        final HttpSession session = httpServletRequest.getSession(true);
        try {
            //
            // attempts to retrieve and validate the access token from session, if this fails the user must login again
            //
            Credentials credentials = Utilities.objectMapper.readValue((String) session.getAttribute(CREDENTIALS), Credentials.class);
            final String accessToken = credentials.getAccessToken();
            return validateAccessToken(accessToken);
        } catch (Exception e) {
            logger.info("Unable to authenticate user using session, attempting to redirect requester to authorize URL for logging in, error=" + e.getMessage());
            final String state = secureRandomString();
            session.setAttribute(STATE, state);
            String redirectUri = httpServletRequest.getHeader(X_AUTH_REQUEST_REDIRECT);
            if (redirectUri == null) {
                redirectUri = httpServletRequest.getRequestURI();
            }
            if (redirectUri != null) {
                session.setAttribute(X_AUTH_REQUEST_REDIRECT, redirectUri);
            }
            //
            // store a randomly generated state into the session before redirecting the requester to login with our Idp
            // when the login flow is complete, our /callback endpoint will validate the state generated here with the one
            // passed back to us via the Idp redirect
            //
            String callback = System.getenv("CALLBACK");
            String audience = System.getenv("AUDIENCE");
            String issuer = System.getenv("ISSUER");
            String scope = "openid profile email";
            String clientId = System.getenv("CLIENT_ID");
            final String authorizeUrl = issuer
                    + "authorize?response_type=code&audience=" + audience
                    + "&state=" + state
                    + "&client_id=" + clientId
                    + "&redirect_uri=" + callback
                    + "&scope=" + scope;
            try {
                httpServletResponse.sendRedirect(authorizeUrl);
                return null;
            } catch (Exception e1) {
                throw new UnauthenticatedException(e1);
            }
        }
    }

    /**
     * Generates a new random string using {@link SecureRandom}.
     * The output can be used as State or Nonce values for API requests.
     *
     * @return a new random string.
     */
    private static String secureRandomString() {
        final SecureRandom sr = new SecureRandom();
        final byte[] randomBytes = new byte[32];
        sr.nextBytes(randomBytes);
        return Base64.encodeBase64URLSafeString(randomBytes);
    }

    /**
     * Validates the access token provided using the trusted issuer
     *
     * @param accessToken the token to validate
     * @return the validated principal
     */
    private DecodedJWT validateAccessToken(String accessToken) throws UnauthenticatedException {
        try {
            return jwtValidator.decodeAndVerify(accessToken);
        } catch (Exception e) {
            throw new UnauthenticatedException(e);
        }
    }

}
