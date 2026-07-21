package com.project.arbaz.aaspass.repository;

import com.project.arbaz.aaspass.entity.EventSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventSeatRepository extends JpaRepository<EventSeat, Long> {
    Optional<EventSeat> findByEventEventId(Long eventId);
}
