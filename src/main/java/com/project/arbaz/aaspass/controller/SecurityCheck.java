package com.project.arbaz.aaspass.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;


/*
    FLOW :
        SecurityFilters --> oAuth2 Redirect Google --> then code --> then token
            --> validation of id token --> loads user info (from google rep)
            --> creates authentication object --> store in http session
            --> tomcat sends the jsessionid cookie

    DefaultOidcuser --> oauth2authenticationtoken --> securitycontext --> httpsession
    Now :
        Spring loads user info from our database -->
        save/update user in postgres --> load roles --> create a custom oidcuser with roles
        --> oauth2authenticationtoken --> securitycontext --> httpsession
 */

@RestController
public class SecurityCheck {
    // Now creating a request and response
    @GetMapping("/request")
    Map<String, Object> getSecurityContext(HttpServletRequest request) {
        return Map.of(
                "method", request.getMethod(),
                "uri", request.getRequestURI(),
                "sessionId", request.getRequestedSessionId() != null ? request.getRequestedSessionId() : "Session Required"
        );
    }

    @GetMapping("/request/add")
    public String getRequest(HttpServletRequest request) {
        return "Hey Arbaz You have to Authenticated";
    }
}
