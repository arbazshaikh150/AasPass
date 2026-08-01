package com.project.arbaz.aaspass.service;

import com.project.arbaz.aaspass.dto.EventSearchResponse;
import com.project.arbaz.aaspass.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SearchService {
    private static final int MAX_QUERY_LENGTH = 100;
    private static final Pattern CONTROL_CHARS =
            Pattern.compile("\\p{Cntrl}");
    private final EventRepository eventRepository;

    public String validate(String query) {
        if (query == null) {
            throw new IllegalArgumentException("Search query cannot be null.");
        }
        // Remove leading/trailing whitespace
        query = query.trim();

        // Reject empty query
        if (query.isEmpty()) {
            throw new IllegalArgumentException("Search query cannot be empty.");
        }

        // Prevent very large inputs
        if (query.length() > MAX_QUERY_LENGTH) {
            throw new IllegalArgumentException(
                    "Search query cannot exceed " + MAX_QUERY_LENGTH + " characters.");
        }

        // Remove control characters
        query = CONTROL_CHARS.matcher(query).replaceAll("");

        // Replace multiple spaces with one
        query = query.replaceAll("\\s+", " ");

        return query;
    }
    // Using the ts vector and gin indexing search
    public List<EventSearchResponse> search(String query) {

        query = validate(query);

        return eventRepository.searchEvents(query)
                .stream()
                .map(event -> new EventSearchResponse(
                        event.getEventId(),
                        event.getEventName(),
                        event.getCreatedAt(),
                        event.getDescriptionText(),
                        event.getHighlightedTags(),
                        event.getImageUrls(),
                        event.getType(),
                        event.getLatitude(),
                        event.getLongitude()
                ))
                .toList();
    }
}
