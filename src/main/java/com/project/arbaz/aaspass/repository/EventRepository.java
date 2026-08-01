package com.project.arbaz.aaspass.repository;

import com.project.arbaz.aaspass.entity.Events;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Events, Long> {

    @Query(value = """
            SELECT *
            FROM events
            WHERE search_vector @@ plainto_tsquery('english', :query)
            ORDER BY ts_rank(
                search_vector,
                plainto_tsquery('english', :query)
            ) DESC
            """,
            nativeQuery = true)
    List<Events> searchEvents(@Param("query") String query);

}
