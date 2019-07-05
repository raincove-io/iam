package io.github.erfangc.iam.authz.controllers;

import io.github.erfangc.iam.authz.models.AuthorizeResponse;
import io.github.erfangc.iam.authz.services.AccessRequest;
import io.github.erfangc.iam.authz.services.AuthorizeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

import static io.github.erfangc.iam.Utilities.SUB;
import static org.springframework.http.MediaType.*;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping("/iam/api/v1")
public class AuthorizeController {
    private AuthorizeService authorizeService;

    public AuthorizeController(AuthorizeService authorizeService) {
        this.authorizeService = authorizeService;
    }

    @RequestMapping(
            method = GET,
            path = "/_authorize",
            consumes = {
                    APPLICATION_JSON_VALUE,
                    TEXT_HTML_VALUE,
                    TEXT_PLAIN_VALUE,
                    APPLICATION_FORM_URLENCODED_VALUE
            }
    )

    @ResponseBody
    public ResponseEntity<AuthorizeResponse> authorizeRequest(HttpServletRequest httpServletRequest) {
        String resource = httpServletRequest.getHeader("X-Auth-Request-Redirect");
        String action = httpServletRequest.getHeader("X-Original-Method");
        String sub = httpServletRequest.getAttribute(SUB).toString();
        AccessRequest accessRequest = new AccessRequest().setAction(action).setResource(resource).setSub(sub);
        final AuthorizeResponse response = authorizeService.authorizeRequest(accessRequest);
        if (response.getAllowed()) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }
    }

}
