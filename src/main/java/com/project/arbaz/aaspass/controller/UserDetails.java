package com.project.arbaz.aaspass.controller;

import com.project.arbaz.aaspass.dto.EventDetailsResponse;
import com.project.arbaz.aaspass.dto.LocationRequest;
import com.project.arbaz.aaspass.dto.NearbyLocationResponse;
import com.project.arbaz.aaspass.security.AppOidcUser;
import com.project.arbaz.aaspass.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserDetails {
    private final UserService userService;

    public UserDetails(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me/events")
    public Page<EventDetailsResponse> getCurrentUserEvents(
            @AuthenticationPrincipal AppOidcUser currentUser,
            @RequestParam(defaultValue = "0") int page
    ) {
        return userService.getEventsCreatedByCurrentUser(currentUser, page);
    }

    @PutMapping("/me/location")
    public void updateCurrentUserLocation(
            @AuthenticationPrincipal AppOidcUser currentUser,
            @RequestBody LocationRequest request
    ) {
        userService.updateCurrentUserLocation(currentUser, request);
    }

    @GetMapping("/me/feed")
    public List<NearbyLocationResponse> getCurrentUserFeed(
            @AuthenticationPrincipal AppOidcUser currentUser,
            @RequestParam(defaultValue = "5") Double radiusKm,
            @RequestParam(defaultValue = "50") Long limit
    ) {
        return userService.getCurrentUserFeed(currentUser, radiusKm, limit);
    }
}
