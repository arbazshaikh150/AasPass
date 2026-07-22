package com.project.arbaz.aaspass.service;

import com.project.arbaz.aaspass.dto.LocationRequest;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Service
public class UserLocationService {
    static final String USER_GEO_INDEX_KEY = "aaspass:users:geo";
    private static final String USER_LOCATION_KEY_PREFIX = "aaspass:users:location:";
    private static final Duration USER_LOCATION_TTL = Duration.ofSeconds(6000);

    private final StringRedisTemplate redisTemplate;

    public UserLocationService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void updateUserLocation(Long userId, Double latitude, Double longitude) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User id is required");
        }
        validateCoordinates(latitude, longitude);

        try {
            String locationKey = locationKey(userId);
            redisTemplate.opsForHash().putAll(locationKey, Map.of(
                    "latitude", latitude.toString(),
                    "longitude", longitude.toString()
            ));
            redisTemplate.expire(locationKey, USER_LOCATION_TTL);
            redisTemplate.opsForGeo().add(USER_GEO_INDEX_KEY, new Point(longitude, latitude), userId.toString());
        } catch (RedisConnectionFailureException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Location service is temporarily unavailable");
        }
    }

    public boolean isUserLocationActive(Long userId) {
        if (userId == null) {
            return false;
        }

        return Boolean.TRUE.equals(redisTemplate.hasKey(locationKey(userId)));
    }

    public Optional<LocationRequest> getUserLocation(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }

        try {
            Map<Object, Object> location = redisTemplate.opsForHash().entries(locationKey(userId));
            if (location.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(new LocationRequest(
                    Double.valueOf(location.get("latitude").toString()),
                    Double.valueOf(location.get("longitude").toString())
            ));
        } catch (RedisConnectionFailureException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Location service is temporarily unavailable");
        }
    }

    static String locationKey(Long userId) {
        return USER_LOCATION_KEY_PREFIX + userId;
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
}
