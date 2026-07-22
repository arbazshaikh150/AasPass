package com.project.arbaz.aaspass.service;

import com.project.arbaz.aaspass.dto.NearbyLocationResponse;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
// TODO : STORE THE TTL FOR THE GEOHASHED KEYS --> TTL WOULD BE THE DURATION OF THE EVENT
@Service
public class GeoIndexService {
    private static final String EVENT_GEO_INDEX_KEY = "aaspass:events:geo";
    private static final double DEFAULT_RADIUS_KM = 5.0;
    private static final long DEFAULT_LIMIT = 50L;

    private final StringRedisTemplate redisTemplate;

    public GeoIndexService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void indexEvent(Long eventId, Double latitude, Double longitude) {
        indexLocation(EVENT_GEO_INDEX_KEY, eventId, latitude, longitude);
    }

    public void removeEvent(Long eventId) {
        removeLocation(EVENT_GEO_INDEX_KEY, eventId);
    }

    public List<NearbyLocationResponse> findNearbyEvents(Double latitude, Double longitude, Double radiusKm, Long limit) {
        return findNearby(EVENT_GEO_INDEX_KEY, latitude, longitude, radiusKm, limit, false);
    }

    public void indexUser(Long userId, Double latitude, Double longitude) {
        indexLocation(UserLocationService.USER_GEO_INDEX_KEY, userId, latitude, longitude);
    }

    public void removeUser(Long userId) {
        removeLocation(UserLocationService.USER_GEO_INDEX_KEY, userId);
    }

    public List<NearbyLocationResponse> findNearbyUsers(Double latitude, Double longitude, Double radiusKm, Long limit) {
        return findNearby(UserLocationService.USER_GEO_INDEX_KEY, latitude, longitude, radiusKm, limit, true);
    }

    private void indexLocation(String indexKey, Long id, Double latitude, Double longitude) {
        if (id == null) {
            return;
        }
        if (latitude == null || longitude == null) {
            removeLocation(indexKey, id);
            return;
        }

        validateCoordinates(latitude, longitude);

        String member = id.toString();
        geoOps().add(indexKey, new Point(longitude, latitude), member);
    }

    private void removeLocation(String indexKey, Long id) {
        if (id == null) {
            return;
        }

        redisTemplate.opsForZSet().remove(indexKey, id.toString());
    }

    private List<NearbyLocationResponse> findNearby(
            String indexKey,
            Double latitude,
            Double longitude,
            Double radiusKm,
            Long limit,
            boolean onlyActiveUsers
    ) {
        validateCoordinates(latitude, longitude);

        double effectiveRadiusKm = radiusKm != null ? radiusKm : DEFAULT_RADIUS_KM;
        long effectiveLimit = limit != null ? limit : DEFAULT_LIMIT;
        if (effectiveRadiusKm <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Radius must be greater than 0");
        }
        if (effectiveLimit <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Limit must be greater than 0");
        }

        Circle searchArea = new Circle(new Point(longitude, latitude), new Distance(effectiveRadiusKm, Metrics.KILOMETERS));
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeCoordinates()
                .includeDistance()
                .sortAscending()
                .limit(effectiveLimit);

        var results = geoOps().radius(indexKey, searchArea, args);
        if (results == null) {
            return List.of();
        }

        return results.getContent().stream()
                .map(result -> {
                    RedisGeoCommands.GeoLocation<String> location = result.getContent();
                    Point point = location.getPoint();
                    String member = location.getName();
                    Long id = Long.valueOf(member);
                    if (onlyActiveUsers && !Boolean.TRUE.equals(redisTemplate.hasKey(UserLocationService.locationKey(id)))) {
                        return null;
                    }
                    return new NearbyLocationResponse(
                            id,
                            point != null ? point.getX() : null,
                            point != null ? point.getY() : null,
                            result.getDistance() != null ? result.getDistance().getValue() : null,
                            redisTemplate.opsForZSet().score(indexKey, member)
                    );
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private void validateCoordinates(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Latitude and longitude are required");
        }
        if (latitude < -90 || latitude > 90) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Latitude must be between -90 and 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Longitude must be between -180 and 180");
        }
    }

    private GeoOperations<String, String> geoOps() {
        return redisTemplate.opsForGeo();
    }
}
