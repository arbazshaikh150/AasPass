package com.project.arbaz.aaspass.controller;

import com.project.arbaz.aaspass.dto.NearbyLocationResponse;
import com.project.arbaz.aaspass.service.GeoIndexService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/geo")
public class GeoController {
    private final GeoIndexService geoIndexService;

    public GeoController(GeoIndexService geoIndexService) {
        this.geoIndexService = geoIndexService;
    }

    @GetMapping("/events/nearby")
    public List<NearbyLocationResponse> findNearbyEvents(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5") Double radiusKm,
            @RequestParam(defaultValue = "50") Long limit
    ) {
        return geoIndexService.findNearbyEvents(latitude, longitude, radiusKm, limit);
    }

    @GetMapping("/users/nearby")
    public List<NearbyLocationResponse> findNearbyUsers(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5") Double radiusKm,
            @RequestParam(defaultValue = "50") Long limit
    ) {
        return geoIndexService.findNearbyUsers(latitude, longitude, radiusKm, limit);
    }
}
