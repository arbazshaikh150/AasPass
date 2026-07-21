package com.project.arbaz.aaspass.service;

import com.project.arbaz.aaspass.dto.EventDetailsResponse;
import com.project.arbaz.aaspass.entity.EventSeat;
import com.project.arbaz.aaspass.entity.EventUser;
import com.project.arbaz.aaspass.entity.Events;
import com.project.arbaz.aaspass.enums.EventType;
import com.project.arbaz.aaspass.repository.EventSeatRepository;
import com.project.arbaz.aaspass.repository.EventUserRepository;
import com.project.arbaz.aaspass.security.AppOidcUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
    private static final int USER_EVENTS_PAGE_SIZE = 10;

    private final EventUserRepository eventUserRepository;
    private final EventSeatRepository eventSeatRepository;

    public UserService(
            EventUserRepository eventUserRepository,
            EventSeatRepository eventSeatRepository
    ) {
        this.eventUserRepository = eventUserRepository;
        this.eventSeatRepository = eventSeatRepository;
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
