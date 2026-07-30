package com.project.arbaz.aaspass.service;

import com.project.arbaz.aaspass.dto.NearbyLocationResponse;
import com.project.arbaz.aaspass.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.project.arbaz.aaspass.constants.Defaults.DEFAULT_USER_LIMIT;
import static com.project.arbaz.aaspass.constants.Defaults.DEFAULT_USER_RADIUS_KM;

@Service
public class NotificationService {
    private final GeoIndexService geoIndexService;
    private final UserRepository userRepository;
    public NotificationService(GeoIndexService geoIndexService ,  UserRepository userRepository) {
        this.geoIndexService = geoIndexService;
        this.userRepository = userRepository;
    }

    public void notify(Double latitude, Double longitude) {
        // Finding the users and then finding there emails and then emailService will take care of the events
        // One database call ( select user_email from userService where (user_id in (list of the user ids )
        // then email service will do the batch processing of the events
        List<NearbyLocationResponse> users = getNearbyUsers(latitude, longitude);
        List<Long> userIds = users.stream().map(NearbyLocationResponse::id).toList();

        List<String> userEmails = userRepository.findEmailsByUserIdIn(userIds);
        // Now using email service for sending the emails
    }

    public List<NearbyLocationResponse> getNearbyUsers( Double latitude, Double longitude) {
        return geoIndexService.findNearbyUsers(latitude , longitude , DEFAULT_USER_RADIUS_KM , DEFAULT_USER_LIMIT);
    }

}
