package io.github.erfangc.iam.authn;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.SecureRandom;

import static io.github.erfangc.iam.Utilities.STATE;
import static io.github.erfangc.iam.Utilities.X_AUTH_REQUEST_REDIRECT;
import static java.util.Collections.singletonList;

/**
 * The {@link LoginLogoutController} handles calls to <code>/login</code> and <code>/logout</code>
 * <p>
 * <code>/login</code> will redirect to IdP login screen while <code>/logout</code> will do the same to the Idp logout page
 */
@Controller
@RequestMapping("/iam/api/v1")
public class LoginLogoutController {
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

    @RequestMapping(method = RequestMethod.GET, path = "login")
    public ResponseEntity<?> login(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        //
        // Construct the OAuth 2.0 Authorization Code Exchange URL to initialize login
        //
        final String state = secureRandomString();
        session.setAttribute(STATE, state);
        String originalRequestUri = request.getParameter("originalRequestUri");
        if (originalRequestUri == null) {
            originalRequestUri = "/home";
        }
        session.setAttribute(X_AUTH_REQUEST_REDIRECT, originalRequestUri);
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
        HttpHeaders headers = new HttpHeaders();
        headers.put("Location", singletonList(authorizeUrl));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @RequestMapping(method = RequestMethod.GET, path = "logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        final HttpSession session = request.getSession();
        session.invalidate();
        HttpHeaders headers = new HttpHeaders();
        String issuer = System.getenv("ISSUER");
        String logoutUrl = issuer + "logout";
        headers.put("Location", singletonList(logoutUrl));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

}
