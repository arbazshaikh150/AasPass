package com.project.arbaz.aaspass.controller;

import com.project.arbaz.aaspass.dto.CreateEventRequest;
import com.project.arbaz.aaspass.dto.EventDetailsResponse;
import com.project.arbaz.aaspass.dto.NearbyLocationResponse;
import com.project.arbaz.aaspass.dto.UpdateEventRequest;
import com.project.arbaz.aaspass.security.AppOidcUser;
import com.project.arbaz.aaspass.service.EventService;
import com.project.arbaz.aaspass.service.GeoIndexService;
import com.project.arbaz.aaspass.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventDetails {
    private final EventService eventService;
    public final NotificationService notificationService;

    public EventDetails(EventService eventService ,  NotificationService notificationService) {
        this.eventService = eventService;
        this.notificationService = notificationService;
    }

    @GetMapping("/{eventId}")
    public EventDetailsResponse findByEventId(@PathVariable Long eventId) {
        return eventService.findByEventId(eventId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventDetailsResponse createEvent(
            @RequestBody CreateEventRequest request,
            @AuthenticationPrincipal AppOidcUser currentUser
    ) {
        return eventService.createEvent(request, currentUser);
    }

    @PutMapping("/{eventId}")
    public EventDetailsResponse updateEvent(
            @PathVariable Long eventId,
            @RequestBody UpdateEventRequest request,
            @AuthenticationPrincipal AppOidcUser currentUser
    ) {
        return eventService.updateEvent(eventId, request, currentUser);
    }

    @DeleteMapping("/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(
            @PathVariable Long eventId,
            @AuthenticationPrincipal AppOidcUser currentUser
    ) {
        eventService.deleteEvent(eventId, currentUser);
    }

    // Fetching the nearby user's for the given event
    @GetMapping("/{latitude}/{longitude}/getUser")
    public List<NearbyLocationResponse> getNearbyUser(@PathVariable Double longitude , @PathVariable Double latitude, @AuthenticationPrincipal AppOidcUser currentUser) {
        return notificationService.getNearbyUsers(latitude , longitude);
    }

    @PostMapping("/notify")
    public ResponseEntity<?> notifyEvent(@RequestBody Map<String, Double> request , @AuthenticationPrincipal AppOidcUser currentUser) {
        notificationService.notify(
                request.get("latitude"),
                request.get("longitude"));
        return ResponseEntity.ok("Notification is sent");
    }

}
