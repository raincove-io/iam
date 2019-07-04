package io.github.erfangc.iam;

import com.fasterxml.jackson.core.JsonProcessingException;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.erfangc.iam.Utilities.*;
import static java.util.Collections.singletonList;
import static org.springframework.util.CollectionUtils.toMultiValueMap;

@Controller
@RequestMapping("/iam/api/v1/callback")
public class CallbackController {

    private Auth0Client auth0Client;

    public CallbackController() {
        auth0Client = Feign
                .builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .target(Auth0Client.class, System.getenv("ISSUER"));
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<?> callback(HttpServletRequest httpServletRequest) throws JsonProcessingException {
        final HttpSession httpSession = authorizationCodeExchange(httpServletRequest);
        Map<String, List<String>> mvm = new HashMap<>();
        String redirectUrl = "/";
        if (httpSession.getAttribute(X_ORIGINAL_URI) != null) {
            redirectUrl = ((String) httpSession.getAttribute(X_ORIGINAL_URI));
        }
        mvm.put("Location", singletonList(redirectUrl));
        MultiValueMap<String, String> headers = toMultiValueMap(mvm);
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    private HttpSession authorizationCodeExchange(HttpServletRequest httpServletRequest) throws JsonProcessingException {
        //
        // gather all the pieces of information needed to exchange code for token
        //
        final String callback = System.getenv("CALLBACK");
        final String clientId = System.getenv("CLIENT_ID");
        final String clientSecret = System.getenv("CLIENT_SECRET");
        final String code = httpServletRequest.getParameter("code");
        final String state = httpServletRequest.getParameter("state");

        final HttpSession session = httpServletRequest.getSession();
        String sessionState = (String) session.getAttribute(STATE);
        if (sessionState == null || !sessionState.equals(state)) {
            throw new IllegalStateException("Session is in an invalid state");
        }
        //
        // perform the actual code exchange for a token, store the token in session
        //
        final Credentials credentials = auth0Client.exchangeCode(
                "authorization_code",
                clientId,
                clientSecret,
                code,
                callback
        );
        session.removeAttribute(STATE);
        session.setAttribute(CREDENTIALS, objectMapper.writeValueAsString(credentials));
        return session;
    }
}
