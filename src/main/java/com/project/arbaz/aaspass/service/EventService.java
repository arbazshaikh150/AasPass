package com.project.arbaz.aaspass.service;

import com.project.arbaz.aaspass.dto.CreateEventRequest;
import com.project.arbaz.aaspass.dto.EventDetailsResponse;
import com.project.arbaz.aaspass.dto.UpdateEventRequest;
import com.project.arbaz.aaspass.entity.EventSeat;
import com.project.arbaz.aaspass.entity.EventUser;
import com.project.arbaz.aaspass.entity.Events;
import com.project.arbaz.aaspass.entity.Users;
import com.project.arbaz.aaspass.enums.EventType;
import com.project.arbaz.aaspass.repository.EventRepository;
import com.project.arbaz.aaspass.repository.EventSeatRepository;
import com.project.arbaz.aaspass.repository.EventUserRepository;
import com.project.arbaz.aaspass.repository.UserRepository;
import com.project.arbaz.aaspass.security.AppOidcUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class EventService {
    private final EventRepository eventRepository;
    private final EventSeatRepository eventSeatRepository;
    private final EventUserRepository eventUserRepository;
    private final UserRepository userRepository;

    public EventService(
            EventRepository eventRepository,
            EventSeatRepository eventSeatRepository,
            EventUserRepository eventUserRepository,
            UserRepository userRepository
    ) {
        this.eventRepository = eventRepository;
        this.eventSeatRepository = eventSeatRepository;
        this.eventUserRepository = eventUserRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public EventDetailsResponse findByEventId(Long eventId) {
        Events event = findEventOrThrow(eventId);
        EventSeat eventSeat = eventSeatRepository.findByEventEventId(eventId).orElse(null);
        EventUser eventUser = eventUserRepository.findByEventEventId(eventId).orElse(null);

        return toResponse(event, eventSeat, eventUser);
    }

    @Transactional
    public EventDetailsResponse createEvent(CreateEventRequest request, AppOidcUser currentUser) {
        validateCreateRequest(request);

        Users user = getCurrentUserReference(currentUser);

        Events event = new Events();
        event.setEventName(request.eventName());
        event.setDescriptionText(request.descriptionText());
        event.setHighlightedTags(new ArrayList<>(request.highlightedTags()));
        // Here i have to upload on the s3 bucket or something
        // i have to read it and then find the suitable method for doing it so
        event.setImageUrls(new ArrayList<>(request.imageUrls() != null ? request.imageUrls() : List.of()));
        event.setType(request.type());
        event.setLatitude(request.latitude());
        event.setLongitude(request.longitude());
        Events savedEvent = eventRepository.save(event);
        EventSeat savedEventSeat = null;

        // Only for the paid users
        if (request.type() != null && request.type() == EventType.PAID) {
            EventSeat eventSeat = new EventSeat();
            eventSeat.setEvent(savedEvent);
            eventSeat.setNumberOfSeats(request.numberOfSeats());
            savedEventSeat = eventSeatRepository.save(eventSeat);
        }

        EventUser eventUser = new EventUser();
        eventUser.setEvent(savedEvent);
        eventUser.setUser(user);
        EventUser savedEventUser = eventUserRepository.save(eventUser);

        return toResponse(savedEvent, savedEventSeat, savedEventUser);
    }

    @Transactional
    public EventDetailsResponse updateEvent(Long eventId, UpdateEventRequest request, AppOidcUser currentUser) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event update request is required");
        }
        Events event = findEventOrThrow(eventId);
        EventUser eventUser = findEventUserOrThrow(eventId);

        validateEventCreator(eventUser, currentUser);

        if (request.eventName() != null) {
            event.setEventName(request.eventName());
        }
        if (request.descriptionText() != null) {
            event.setDescriptionText(request.descriptionText());
        }
        if (request.highlightedTags() != null) {
            event.setHighlightedTags(new ArrayList<>(request.highlightedTags()));
        }
        if (request.imageUrls() != null) {
            event.setImageUrls(new ArrayList<>(request.imageUrls()));
        }
        if (request.type() != null) {
            event.setType(request.type());
        }
        if (request.latitude() != null) {
            event.setLatitude(request.latitude());
        }
        if (request.longitude() != null) {
            event.setLongitude(request.longitude());
        }

        Events savedEvent = eventRepository.save(event);
        EventSeat eventSeat = eventSeatRepository.findByEventEventId(eventId).orElse(null);

        if (request.numberOfSeats() != null) {
            if (request.numberOfSeats() < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Number of seats cannot be negative");
            }
            if (eventSeat == null) {
                eventSeat = new EventSeat();
                eventSeat.setEvent(savedEvent);
            }
            eventSeat.setNumberOfSeats(request.numberOfSeats());
            eventSeat = eventSeatRepository.save(eventSeat);
        }

        return toResponse(savedEvent, eventSeat, eventUser);
    }

    @Transactional
    public void deleteEvent(Long eventId, AppOidcUser currentUser) {
        Events event = findEventOrThrow(eventId);
        EventUser eventUser = findEventUserOrThrow(eventId);

        validateEventCreator(eventUser, currentUser);

        eventSeatRepository.findByEventEventId(eventId).ifPresent(eventSeatRepository::delete);
        eventUserRepository.delete(eventUser);
        eventRepository.delete(event);
    }

    private Events findEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
    }

    private EventUser findEventUserOrThrow(Long eventId) {
        return eventUserRepository.findByEventEventId(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event creator not found"));
    }

    private void validateCreateRequest(CreateEventRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event create request is required");
        }
        if (request.eventName() == null || request.eventName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event name is required");
        }
        if (request.type() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event type is required");
        }
        if (request.type() == EventType.PAID && (request.numberOfSeats() == null || request.numberOfSeats() < 0)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valid number of seats is required for paid events");
        }
        if (request.highlightedTags() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Highlighted tags are required");
        }
    }

    private Users getCurrentUserReference(AppOidcUser currentUser) {
        if (currentUser == null || currentUser.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated app user is required");
        }

        return userRepository.getReferenceById(currentUser.getUserId());
    }

    private void validateEventCreator(EventUser eventUser, AppOidcUser currentUser) {
        if (currentUser == null || currentUser.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated app user is required");
        }
        if (!Objects.equals(currentUser.getUserId(), eventUser.getUser().getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the event creator can modify this event");
        }
    }

    private EventDetailsResponse toResponse(Events event, EventSeat eventSeat, EventUser eventUser) {
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
                eventUser != null ? eventUser.getUser().getUserId() : null
        );
    }
}
