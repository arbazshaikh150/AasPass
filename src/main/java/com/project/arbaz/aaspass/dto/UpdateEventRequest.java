package com.project.arbaz.aaspass.dto;

import com.project.arbaz.aaspass.enums.EventType;

import java.util.List;

public record UpdateEventRequest(
        String eventName,
        String descriptionText,
        List<String> highlightedTags,
        List<String> imageUrls,
        EventType type,
        Double latitude,
        Double longitude,
        Long numberOfSeats
) {
}
