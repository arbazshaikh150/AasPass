package com.project.arbaz.aaspass.repository;

import com.project.arbaz.aaspass.entity.EventUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventUserRepository extends JpaRepository<EventUser, Long> {
    Optional<EventUser> findByEventEventId(Long eventId);
}
