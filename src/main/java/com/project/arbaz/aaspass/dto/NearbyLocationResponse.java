package com.project.arbaz.aaspass.dto;

public record NearbyLocationResponse(
        Long id,
        Double longitude,
        Double latitude,
        Double distanceKm,
        Double score
) {
}
