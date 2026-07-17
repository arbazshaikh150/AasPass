package com.project.arbaz.aaspass.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

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
