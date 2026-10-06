package com.eventticketing.eventservice.service.impl;

import com.eventticketing.eventservice.dto.request.CreateEventRequestDto;
import com.eventticketing.eventservice.dto.request.UpdateEventRequestDto;
import com.eventticketing.eventservice.dto.response.EventResponseDto;
import com.eventticketing.eventservice.dto.response.paginate.EventPaginateResponseDto;
import com.eventticketing.eventservice.entity.Event;
import com.eventticketing.eventservice.enums.EventStatus;
import com.eventticketing.eventservice.exception.InvalidEventStateException;
import com.eventticketing.eventservice.exception.ResourceNotFoundException;
import com.eventticketing.eventservice.exception.UnauthorizedAccessException;
import com.eventticketing.eventservice.exception.VenueConflictException;
import com.eventticketing.eventservice.mapper.ObjectMapper;
import com.eventticketing.eventservice.repository.EventRepository;
import com.eventticketing.eventservice.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    @CacheEvict(value = "event_list", allEntries = true)
    public EventResponseDto createEvent(CreateEventRequestDto request, UUID organizerId) {

        LocalDateTime startTime = request.getEventDateTime().minusHours(1);
        LocalDateTime endTimeWithDuration = request.getEventDateTime()
                .plusHours(request.getDurationInHours())
                .plusHours(1);

        if(eventRepository.existsVenueConflict(request.getVenue(), startTime,endTimeWithDuration)){
            throw new VenueConflictException("The venue '" + request.getVenue() + "' is already booked during the requested time slot.");
        }

        LocalDateTime endTime = request.getEventDateTime().plusHours(request.getDurationInHours());

        Event event = Event.builder()
                     .title(request.getTitle())
                     .description(request.getDescription())
                     .venue(request.getVenue())
                     .eventDateTime(request.getEventDateTime())
                     .totalTickets(request.getTotalTickets())
                     .availableTickets(request.getTotalTickets())
                     .ticketPrice(request.getTicketPrice())
                     .endDateTime(endTime)
                     .organizerId(organizerId)
                     .build();

        Event savedEvent = eventRepository.save(event);
        log.info("Event successfully created with ID: {}", savedEvent.getId());

        return objectMapper.toEventResponse(savedEvent);

    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "event", key = "#id")
    public EventResponseDto getEventById(UUID id) {
        log.debug("Fetching event details from Database for ID: {}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        return objectMapper.toEventResponse(event);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = "event_list",
            key = "'page:' + #page + ':size:' + #size"
    )
    public EventPaginateResponseDto getAllUpComingEvents(int page, int size) {
        log.debug("Fetching all upcoming events from Database");
        LocalDateTime now = LocalDateTime.now();

        Page<Event> eventPage = eventRepository
                .findEventByEventDateRange(now, PageRequest.of(page, size));

        return EventPaginateResponseDto.builder()
                .dataCount(eventPage.getTotalElements())
                .dataList(eventPage.getContent().stream()
                        .map(objectMapper::toEventResponse)
                        .collect(Collectors.toList()))
                .build();

    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "event", key = "#id"),
            @CacheEvict(value = "event_list", allEntries = true)
    })
    public EventResponseDto updateEvent(UUID id, UpdateEventRequestDto request, UUID organizerId) {
        log.info("Updating event ID: {} by organizer ID: {}", id, organizerId);
        Event selectedEvent = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + id));


        if (!selectedEvent.getOrganizerId().equals(organizerId)) {
            throw new UnauthorizedAccessException("You are not authorized to update this event");
        }

        if (selectedEvent.getStatus() == EventStatus.CANCELLED || selectedEvent.getStatus() == EventStatus.COMPLETED) {
            throw new InvalidEventStateException("Cannot update a cancelled or completed event with ID: " + id);
        }

        LocalDateTime startTime = request.getEventDateTime().minusHours(1);
        LocalDateTime endTimeWithDuration = request.getEventDateTime().plusHours(request.getDurationInHours()).plusHours(1);

        if(eventRepository.existsVenueConflictExcludingEvent(request.getVenue(),startTime,endTimeWithDuration,id)){
            throw new VenueConflictException("The venue '" + request.getVenue() + "' is already booked during the requested time slot.");
        }

        LocalDateTime endTime = request.getEventDateTime().plusHours(request.getDurationInHours());

        selectedEvent.setTitle(request.getTitle());
        selectedEvent.setDescription(request.getDescription());
        selectedEvent.setVenue(request.getVenue());
        selectedEvent.setEventDateTime(request.getEventDateTime());
        selectedEvent.setEndDateTime(endTime);
        log.info("Event ID: {} successfully updated", id);

        return objectMapper.toEventResponse(selectedEvent);

    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "event", key = "#id"),
            @CacheEvict(value = "event_list", allEntries = true)
    })
    public void cancelEvent(UUID id, UUID organizerId) {
        log.info("Cancelling event ID: {} by organizer ID: {}", id, organizerId);
        Event selectedEvent = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + id));

        if (!selectedEvent.getOrganizerId().equals(organizerId)) {
            throw new UnauthorizedAccessException("You are not authorized to cancel this event");
        }

        if (selectedEvent.getStatus() == EventStatus.CANCELLED || selectedEvent.getStatus() == EventStatus.COMPLETED) {
            throw new InvalidEventStateException("Cannot delete a cancelled or completed event with ID: " + id);
        }

        selectedEvent.setStatus(EventStatus.CANCELLED);
        log.info("Event ID: {} marked as CANCELLED", id);

    }

}
