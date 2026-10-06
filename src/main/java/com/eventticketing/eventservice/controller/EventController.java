package com.eventticketing.eventservice.controller;

import com.eventticketing.eventservice.dto.request.CreateEventRequestDto;
import com.eventticketing.eventservice.dto.request.UpdateEventRequestDto;
import com.eventticketing.eventservice.dto.response.EventResponseDto;
import com.eventticketing.eventservice.dto.response.paginate.EventPaginateResponseDto;
import com.eventticketing.eventservice.exception.UnauthorizedAccessException;
import com.eventticketing.eventservice.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponseDto> createEvent(
            @Valid @RequestBody CreateEventRequestDto request) {

        UUID organizerId = getAuthenticatedUserId();

        log.info(
                "REST Request to create event by organizer: {}",
                organizerId
        );

        EventResponseDto response =
                eventService.createEvent(request, organizerId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDto> getEventById(
            @PathVariable UUID id) {

        log.debug("REST Request to get event by ID: {}", id);

        EventResponseDto response =
                eventService.getEventById(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<EventPaginateResponseDto> getAllUpcomingEvents(   @RequestParam int page,
                                                                          @RequestParam int size) {

        log.debug("REST Request to get all upcoming events");

        EventPaginateResponseDto responses =
                eventService.getAllUpComingEvents(page, size);

        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDto> updateEvent(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEventRequestDto request) {

        UUID organizerId = getAuthenticatedUserId();

        log.info(
                "REST Request to update event ID: {} by organizer: {}",
                id,
                organizerId
        );

        EventResponseDto response =
                eventService.updateEvent(id, request, organizerId);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelEvent(
            @PathVariable UUID id) {

        UUID organizerId = getAuthenticatedUserId();

        log.info(
                "REST Request to cancel event ID: {} by organizer: {}",
                id,
                organizerId
        );

        eventService.cancelEvent(id, organizerId);

        return ResponseEntity.noContent().build();
    }

    private UUID getAuthenticatedUserId() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new UnauthorizedAccessException(
                    "Authenticated user not found"
            );
        }

        System.out.println("PRINCIPAL " + authentication.getPrincipal().toString());
        return UUID.fromString(
                authentication.getPrincipal().toString()
        );
    }
}