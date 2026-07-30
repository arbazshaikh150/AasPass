package com.project.arbaz.aaspass.dto;

public record EventCreatedEvent(
        Long eventId,
        Double latitude,
        Double longitude
) {
}
