package io.github.erfangc.iam.authz.controllers;

import io.github.erfangc.iam.authz.models.AuthorizeResponse;
import io.github.erfangc.iam.authz.services.AuthorizeService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

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
    public AuthorizeResponse authorizeRequest(HttpServletRequest httpServletRequest) {
        String resource = httpServletRequest.getHeader("X-Auth-Request-Redirect");
        String verb = httpServletRequest.getHeader("X-Original-Method");
        String sub = httpServletRequest.getAttribute(SUB).toString();
        return authorizeService.authorizeRequest(resource, verb, sub);
    }

}
