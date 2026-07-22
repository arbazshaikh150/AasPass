package com.project.arbaz.aaspass.service;

import com.project.arbaz.aaspass.dto.EventDetailsResponse;
import com.project.arbaz.aaspass.dto.LocationRequest;
import com.project.arbaz.aaspass.dto.NearbyLocationResponse;
import com.project.arbaz.aaspass.entity.EventSeat;
import com.project.arbaz.aaspass.entity.EventUser;
import com.project.arbaz.aaspass.entity.Events;
import com.project.arbaz.aaspass.entity.Users;
import com.project.arbaz.aaspass.enums.EventType;
import com.project.arbaz.aaspass.repository.EventSeatRepository;
import com.project.arbaz.aaspass.repository.EventUserRepository;
import com.project.arbaz.aaspass.repository.UserRepository;
import com.project.arbaz.aaspass.security.AppOidcUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {
    private static final int USER_EVENTS_PAGE_SIZE = 10;
    private static final double DEFAULT_FEED_RADIUS_KM = 5.0;
    private static final long DEFAULT_FEED_LIMIT = 50L;

    private final EventUserRepository eventUserRepository;
    private final EventSeatRepository eventSeatRepository;
    private final UserRepository userRepository;
    private final GeoIndexService geoIndexService;
    private final UserLocationService userLocationService;

    public UserService(
            EventUserRepository eventUserRepository,
            EventSeatRepository eventSeatRepository,
            UserRepository userRepository,
            GeoIndexService geoIndexService,
            UserLocationService userLocationService
    ) {
        this.eventUserRepository = eventUserRepository;
        this.eventSeatRepository = eventSeatRepository;
        this.userRepository = userRepository;
        this.geoIndexService = geoIndexService;
        this.userLocationService = userLocationService;
    }

    @Transactional(readOnly = true)
    public Page<EventDetailsResponse> getEventsCreatedByCurrentUser(AppOidcUser currentUser, int page) {
        if (currentUser == null || currentUser.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated app user is required");
        }
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page cannot be negative");
        }

        PageRequest pageRequest = PageRequest.of(
                page,
                USER_EVENTS_PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "event.createdAt")
        );

        return eventUserRepository.findByUserUserId(currentUser.getUserId(), pageRequest)
                .map(this::toResponse);
    }

    @Transactional
    public void updateCurrentUserLocation(AppOidcUser currentUser, LocationRequest request) {
        if (currentUser == null || currentUser.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated app user is required");
        }
        if (request == null || request.latitude() == null || request.longitude() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Latitude and longitude are required");
        }

        Users user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        userLocationService.updateUserLocation(user.getUserId(), request.latitude(), request.longitude());
    }

    public List<NearbyLocationResponse> getCurrentUserFeed(AppOidcUser currentUser, Double radiusKm, Long limit) {
        if (currentUser == null || currentUser.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated app user is required");
        }

        LocationRequest location = userLocationService.getUserLocation(currentUser.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current user location is required"));

        return geoIndexService.findNearbyEvents(
                location.latitude(),
                location.longitude(),
                radiusKm != null ? radiusKm : DEFAULT_FEED_RADIUS_KM,
                limit != null ? limit : DEFAULT_FEED_LIMIT
        );
    }

    private EventDetailsResponse toResponse(EventUser eventUser) {
        Events event = eventUser.getEvent();
        EventSeat eventSeat = null;
        if(event.getType() == EventType.PAID)
            eventSeat = eventSeatRepository.findByEventEventId(event.getEventId()).orElse(null);

        return new EventDetailsResponse(
                event.getEventId(),
                event.getEventName(),
                event.getCreatedAt(),
                event.getDescriptionText(),
                event.getHighlightedTags(),
                event.getImageUrls(),
                event.getType(),
                event.getLatitude(),
                event.getLongitude(),
                eventSeat != null ? eventSeat.getNumberOfSeats() : null,
                eventUser.getUser().getUserId()
        );
    }
}
