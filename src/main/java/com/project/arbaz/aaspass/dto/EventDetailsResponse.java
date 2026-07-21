package com.project.arbaz.aaspass.dto;

import com.project.arbaz.aaspass.enums.EventType;

import java.time.LocalDateTime;
import java.util.List;

public record EventDetailsResponse(
        Long eventId,
        String eventName,
        LocalDateTime createdAt,
        String descriptionText,
        List<String> highlightedTags,
        List<String> imageUrls,
        EventType type,
        Double latitude,
        Double longitude,
        Long numberOfSeats,
        Long createdByUserId
) {
}
