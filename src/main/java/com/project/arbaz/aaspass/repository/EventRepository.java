package com.project.arbaz.aaspass.repository;

import com.project.arbaz.aaspass.entity.Events;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Events, Long> {
}
