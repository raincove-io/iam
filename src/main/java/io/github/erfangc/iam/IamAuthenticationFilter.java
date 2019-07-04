package io.github.erfangc.iam;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private static final String CREDENTIALS = "credentials";
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules().
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        final String authorization = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        final boolean isApiCall = authorization != null && authorization.startsWith("Bearer ");
        String authenticatedPrincipal = null;
        try {
            if (isApiCall) {
                // handle JWT based authentication
                authenticatedPrincipal = validateAccessToken(extractAccessToken(authorization));
            } else {
                // handle session based authentication
                authenticatedPrincipal = validateSession(httpServletRequest);
            }
        } catch (UnauthenticatedException exception) {
            // depending on whether the request has an authorization header we will either return 401 or 302
            if (isApiCall) {
                // 401
                httpServletResponse.setHeader("Content-Type", "application/json");
                httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpServletResponse.getOutputStream().write(("{\"message\":\"Bearer token is not valid or is expired\", \"timestamp\": \"" + Instant.now().toString() + "\"}").getBytes());
                logger.info("Anonymous access prohibited for API calls");
                return;
            } else {
                // 302 (redirect to IdP for login)
                // TODO construct authorize URL
                httpServletResponse.setHeader("Location", "https://raincove.auth0.com/authorize?...");
                httpServletResponse.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
                logger.info("Anonymous access prohibited, redirecting to login URL");
                return;
            }
        }
        logger.info("Authenticated request for principal=" + authenticatedPrincipal);
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    private String extractAccessToken(String authorization) {
        return authorization.replaceFirst("^Bearer ", authorization);
    }

    private String validateSession(HttpServletRequest httpServletRequest) throws UnauthenticatedException {
        final HttpSession session = httpServletRequest.getSession();
        if (session == null) {
            throw new UnauthenticatedException();
        } else {
            try {
                Credentials credentials = objectMapper.readValue((String) session.getAttribute(CREDENTIALS), Credentials.class);
                final String accessToken = credentials.getAccessToken();
                return validateAccessToken(accessToken);
            } catch (Exception e) {
                throw new UnauthenticatedException(e);
            }
        }
    }

    private String validateAccessToken(String accessToken) throws UnauthenticatedException {
        // TODO
        throw new UnsupportedOperationException();
    }
}
