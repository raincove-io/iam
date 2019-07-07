package io.github.erfangc.iam.authn;

import com.auth0.jwt.interfaces.DecodedJWT;
import io.github.erfangc.iam.Utilities;
import io.github.erfangc.iam.authn.models.Credentials;
import io.github.erfangc.iam.authn.models.Operation;
import io.github.erfangc.iam.authz.models.AuthorizeResponse;
import io.github.erfangc.iam.authz.services.AccessRequest;
import io.github.erfangc.iam.authz.services.AuthorizeService;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static io.github.erfangc.iam.Utilities.CREDENTIALS;
import static io.github.erfangc.iam.Utilities.SUB;
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
    private AuthorizeService authorizeService;

    public IamAuthenticationFilter(JwtValidator jwtValidator, AuthorizeService authorizeService) {
        this.jwtValidator = jwtValidator;
        this.authorizeService = authorizeService;
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
        noAuthOperations.add(
                new Operation()
                        .setResource("/iam/api/v1/login")
                        .setVerbs(singleton("GET"))
        );
        noAuthOperations.add(
                new Operation()
                        .setResource("/iam/api/v1/logout")
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
    protected void doFilterInternal(HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {
        //
        // let the callback pass through without authentication
        //
        if (allowUnauthenticated(httpServletRequest)) {
            doFilter(httpServletRequest, httpServletResponse, filterChain);
        } else {
            final String authorization = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
            final boolean isApiCall = authorization != null && authorization.startsWith("Bearer ");
            final String requestURI = httpServletRequest.getRequestURI();
            final String method = httpServletRequest.getMethod();
            String accessToken;
            DecodedJWT decodedJwt;
            try {
                if (isApiCall) {
                    // handle JWT based authentication
                    accessToken = extractAccessToken(authorization);
                } else {
                    // handle session based authentication
                    accessToken = getAccessTokenFromSession(httpServletRequest, httpServletResponse);
                }
                decodedJwt = jwtValidator.decodeAndVerify(accessToken);
                //
                // Request authenticated
                //
                String sub = decodedJwt.getSubject();
                httpServletRequest.setAttribute(SUB, sub);
                logger.info(
                        "Authenticated request"
                                + " type=" + (isApiCall ? "API" : "Web")
                                + " sub=" + sub
                                + " requestUri=" + requestURI
                                + " method=" + method
                );
                //
                // if the request is to authenticate another request, allow it go through, that is the main point of IAM
                // once authenticated everyone is allowed to call the _authorize operation against some resource and action
                // combination
                //
                if (requestURI.startsWith("/iam/api/v1/_authorize")) {
                    filterChain.doFilter(httpServletRequest, httpServletResponse);
                } else {
                    //
                    // now authorize the request
                    //
                    final AuthorizeResponse authorizeResponse = authorizeService.authorizeRequest(
                            new AccessRequest()
                                    .setSub(sub)
                                    .setResource(requestURI)
                                    .setAction(method)
                    );
                    if (authorizeResponse.getAllowed()) {
                        filterChain.doFilter(httpServletRequest, httpServletResponse);
                    } else {
                        handleAuthorizationFailed(httpServletResponse, isApiCall);
                    }
                }
            } catch (UnauthenticatedException e) {
                //
                // Request unauthenticated
                //
                logger.info(
                        "Unable to authenticated request"
                                + " type=" + (isApiCall ? "API" : "Web")
                                + " requestUri=" + requestURI
                                + " method=" + method
                );
                handleAuthenticationFailed(httpServletResponse, isApiCall);
            }
        }
    }

    private void handleAuthenticationFailed(HttpServletResponse httpServletResponse, boolean isApiCall) throws IOException {
        if (isApiCall) {
            String id = UUID.randomUUID().toString();
            httpServletResponse.setHeader("Content-Type", "application/json");
            httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpServletResponse.getOutputStream().write(("{\"id\": \"" + id + "\", \"message\":\"Bearer token is not valid or is expired\", \"timestamp\": \"" + Instant.now().toString() + "\"}").getBytes());
        } else {
            httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private void handleAuthorizationFailed(HttpServletResponse httpServletResponse, boolean isApiCall) throws IOException {
        if (isApiCall) {
            String id = UUID.randomUUID().toString();
            httpServletResponse.setHeader("Content-Type", "application/json");
            httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpServletResponse.getOutputStream().write(("{\"id\": \"" + id + "\", \"message\":\"Forbidden\", \"timestamp\": \"" + Instant.now().toString() + "\"}").getBytes());
        } else {
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    private String extractAccessToken(String authorization) throws UnauthenticatedException {
        try {
            return authorization.replaceFirst("^Bearer ", "");
        } catch (Exception e) {
            throw new UnauthenticatedException("Unable to retrieve access token from Authorization header, error: " + e.getMessage());
        }
    }

    private String getAccessTokenFromSession(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws UnauthenticatedException {
        //
        // create or get a session based on cookie value
        //
        final HttpSession session = httpServletRequest.getSession(true);
        final Object credentials = session.getAttribute(CREDENTIALS);
        if (credentials == null) {
            session.invalidate();
            throw new UnauthenticatedException("Unable to retrieve credentials from session");
        }
        //
        // attempts to retrieve and validate the access token from session, if this fails the user must login again
        //
        try {
            return Utilities.objectMapper.readValue((String) credentials, Credentials.class).getAccessToken();
        } catch (Exception e) {
            session.invalidate();
            throw new UnauthenticatedException("Unable to deserialize access token from session credentials, error: " + e.getMessage());
        }
    }

}
