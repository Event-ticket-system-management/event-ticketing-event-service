package com.eventticketing.eventservice.repository;

import com.eventticketing.eventservice.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
    @Query("""
      SELECT COUNT(e) > 0
      FROM Event e
      WHERE LOWER(e.venue) = LOWER(:venue)
      AND e.eventDateTime < :endTime
      AND e.endDateTime < :startTime
      AND e.status != 'CANCELLED'
    """)
    boolean existsVenueConflict(
            @Param("venue") String venue,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("""
     SELECT e FROM Event e
     WHERE e.eventDateTime >= :now
     AND e.status != 'CANCELLED'
     ORDER BY e.eventDateTime ASC
     """)
    Page<Event> findEventByEventDateRange(@Param("now") LocalDateTime now, Pageable pageable);


    @Query("""
    SELECT COUNT(e) > 0
    FROM Event e
    WHERE LOWER(e.venue) = LOWER(:venue)
    AND e.eventDateTime < :endTime
    AND e.endDateTime > :startTime
    AND e.status != 'CANCELLED'
    AND e.id <> :eventId
""")
    boolean existsVenueConflictExcludingEvent(
            @Param("venue") String venue,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("eventId") UUID eventId
    );

}
