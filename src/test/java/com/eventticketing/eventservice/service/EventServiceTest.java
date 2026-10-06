package com.eventticketing.eventservice.service;

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
import com.eventticketing.eventservice.service.impl.EventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    private UUID eventId;
    private UUID organizerId;
    private UUID anotherOrganizerId;

    private LocalDateTime eventDateTime;

    private Event event;
    private EventResponseDto eventResponseDto;

    @BeforeEach
    void setUp() {

        eventId = UUID.randomUUID();
        organizerId = UUID.randomUUID();
        anotherOrganizerId = UUID.randomUUID();

        eventDateTime = LocalDateTime.now()
                .plusDays(10)
                .withNano(0);

        event = Event.builder()
                .id(eventId)
                .title("Tech Conference 2026")
                .description("Annual technology conference")
                .venue("Main Hall")
                .eventDateTime(eventDateTime)
                .endDateTime(eventDateTime.plusHours(4))
                .totalTickets(500)
                .availableTickets(500)
                .ticketPrice(new BigDecimal("2500.00"))
                .organizerId(organizerId)
                .status(EventStatus.PUBLISHED)
                .build();

        eventResponseDto = EventResponseDto.builder()
                .id(eventId)
                .title("Tech Conference 2026")
                .description("Annual technology conference")
                .venue("Main Hall")
                .eventDateTime(eventDateTime)
                .totalTickets(500)
                .availableTickets(500)
                .ticketPrice(new BigDecimal("2500.00"))
                .organizerId(organizerId)
                .build();
    }

    // =========================================================
    // CREATE EVENT
    // =========================================================

    @Test
    void createEvent_ShouldCreateEvent_WhenVenueIsAvailable() {

        CreateEventRequestDto request = CreateEventRequestDto.builder()
                .title("Tech Conference 2026")
                .description("Annual technology conference")
                .venue("Main Hall")
                .eventDateTime(eventDateTime)
                .durationInHours(4)
                .totalTickets(500)
                .ticketPrice(new BigDecimal("2500.00"))
                .build();

        when(eventRepository.existsVenueConflict(
                eq("Main Hall"),
                eq(eventDateTime.minusHours(1)),
                eq(eventDateTime.plusHours(5))
        )).thenReturn(false);

        when(eventRepository.save(any(Event.class)))
                .thenReturn(event);

        when(objectMapper.toEventResponse(event))
                .thenReturn(eventResponseDto);

        EventResponseDto result =
                eventService.createEvent(request, organizerId);

        assertNotNull(result);
        assertEquals(eventId, result.getId());
        assertEquals("Tech Conference 2026", result.getTitle());
        assertEquals("Main Hall", result.getVenue());
        assertEquals(organizerId, result.getOrganizerId());

        verify(eventRepository).existsVenueConflict(
                "Main Hall",
                eventDateTime.minusHours(1),
                eventDateTime.plusHours(5)
        );

        verify(eventRepository).save(any(Event.class));
        verify(objectMapper).toEventResponse(event);
    }

    @Test
    void createEvent_ShouldThrowVenueConflictException_WhenVenueHasConflict() {

        CreateEventRequestDto request = CreateEventRequestDto.builder()
                .title("Tech Conference 2026")
                .description("Annual technology conference")
                .venue("Main Hall")
                .eventDateTime(eventDateTime)
                .durationInHours(4)
                .totalTickets(500)
                .ticketPrice(new BigDecimal("2500.00"))
                .build();

        when(eventRepository.existsVenueConflict(
                eq("Main Hall"),
                eq(eventDateTime.minusHours(1)),
                eq(eventDateTime.plusHours(5))
        )).thenReturn(true);

        assertThrows(
                VenueConflictException.class,
                () -> eventService.createEvent(request, organizerId)
        );

        verify(eventRepository, never())
                .save(any(Event.class));

        verifyNoInteractions(objectMapper);
    }

    // =========================================================
    // GET EVENT BY ID
    // =========================================================

    @Test
    void getEventById_ShouldReturnEvent_WhenEventExists() {

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(objectMapper.toEventResponse(event))
                .thenReturn(eventResponseDto);

        EventResponseDto result =
                eventService.getEventById(eventId);

        assertNotNull(result);
        assertEquals(eventId, result.getId());
        assertEquals("Tech Conference 2026", result.getTitle());
        assertEquals("Main Hall", result.getVenue());

        verify(eventRepository).findById(eventId);
        verify(objectMapper).toEventResponse(event);
    }

    @Test
    void getEventById_ShouldThrowResourceNotFoundException_WhenEventDoesNotExist() {

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> eventService.getEventById(eventId)
        );

        verify(eventRepository).findById(eventId);
        verifyNoInteractions(objectMapper);
    }

    // =========================================================
    // GET ALL UPCOMING EVENTS
    // =========================================================

    @Test
    void getAllUpComingEvents_ShouldReturnPaginatedEvents() {

        int page = 0;
        int size = 10;

        Page<Event> eventPage = new PageImpl<>(
                List.of(event),
                PageRequest.of(page, size),
                1
        );

        when(eventRepository.findEventByEventDateRange(
                any(LocalDateTime.class),
                eq(PageRequest.of(page, size))
        )).thenReturn(eventPage);

        when(objectMapper.toEventResponse(event))
                .thenReturn(eventResponseDto);

        EventPaginateResponseDto result =
                eventService.getAllUpComingEvents(page, size);

        assertNotNull(result);

        assertEquals(
                1,
                result.getDataCount()
        );

        assertNotNull(result.getDataList());
        assertEquals(1, result.getDataList().size());

        assertEquals(
                eventId,
                result.getDataList().getFirst().getId()
        );

        verify(eventRepository).findEventByEventDateRange(
                any(LocalDateTime.class),
                eq(PageRequest.of(page, size))
        );

        verify(objectMapper).toEventResponse(event);
    }

    // =========================================================
    // UPDATE EVENT
    // =========================================================

    @Test
    void updateEvent_ShouldUpdateEvent_WhenRequestIsValid() {

        LocalDateTime newStartTime =
                eventDateTime.plusDays(5);

        UpdateEventRequestDto request =
                UpdateEventRequestDto.builder()
                        .title("Updated Tech Conference")
                        .description("Updated description")
                        .venue("Conference Hall A")
                        .eventDateTime(newStartTime)
                        .durationInHours(5)
                        .build();

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(eventRepository.existsVenueConflictExcludingEvent(
                eq("Conference Hall A"),
                eq(newStartTime.minusHours(1)),
                eq(newStartTime.plusHours(6)),
                eq(eventId)
        )).thenReturn(false);

        when(objectMapper.toEventResponse(event))
                .thenReturn(eventResponseDto);

        EventResponseDto result =
                eventService.updateEvent(
                        eventId,
                        request,
                        organizerId
                );

        assertNotNull(result);

        assertEquals(
                "Updated Tech Conference",
                event.getTitle()
        );

        assertEquals(
                "Updated description",
                event.getDescription()
        );

        assertEquals(
                "Conference Hall A",
                event.getVenue()
        );

        assertEquals(
                newStartTime,
                event.getEventDateTime()
        );

        assertEquals(
                newStartTime.plusHours(5),
                event.getEndDateTime()
        );

        verify(eventRepository).findById(eventId);

        verify(eventRepository)
                .existsVenueConflictExcludingEvent(
                        "Conference Hall A",
                        newStartTime.minusHours(1),
                        newStartTime.plusHours(6),
                        eventId
                );

        verify(objectMapper).toEventResponse(event);
    }

    @Test
    void updateEvent_ShouldThrowResourceNotFoundException_WhenEventDoesNotExist() {

        UpdateEventRequestDto request =
                createUpdateRequest();

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> eventService.updateEvent(
                        eventId,
                        request,
                        organizerId
                )
        );

        verify(eventRepository).findById(eventId);

        verify(eventRepository, never())
                .existsVenueConflictExcludingEvent(
                        anyString(),
                        any(),
                        any(),
                        any()
                );

        verifyNoInteractions(objectMapper);
    }

    @Test
    void updateEvent_ShouldThrowUnauthorizedAccessException_WhenOrganizerDoesNotOwnEvent() {

        UpdateEventRequestDto request =
                createUpdateRequest();

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        assertThrows(
                UnauthorizedAccessException.class,
                () -> eventService.updateEvent(
                        eventId,
                        request,
                        anotherOrganizerId
                )
        );

        verify(eventRepository).findById(eventId);

        verify(eventRepository, never())
                .existsVenueConflictExcludingEvent(
                        anyString(),
                        any(),
                        any(),
                        any()
                );

        verifyNoInteractions(objectMapper);
    }

    @Test
    void updateEvent_ShouldThrowInvalidEventStateException_WhenEventIsCancelled() {

        event.setStatus(EventStatus.CANCELLED);

        UpdateEventRequestDto request =
                createUpdateRequest();

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        assertThrows(
                InvalidEventStateException.class,
                () -> eventService.updateEvent(
                        eventId,
                        request,
                        organizerId
                )
        );

        verify(eventRepository).findById(eventId);

        verify(eventRepository, never())
                .existsVenueConflictExcludingEvent(
                        anyString(),
                        any(),
                        any(),
                        any()
                );

        verifyNoInteractions(objectMapper);
    }

    @Test
    void updateEvent_ShouldThrowInvalidEventStateException_WhenEventIsCompleted() {

        event.setStatus(EventStatus.COMPLETED);

        UpdateEventRequestDto request =
                createUpdateRequest();

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        assertThrows(
                InvalidEventStateException.class,
                () -> eventService.updateEvent(
                        eventId,
                        request,
                        organizerId
                )
        );

        verify(eventRepository).findById(eventId);

        verifyNoInteractions(objectMapper);
    }

    @Test
    void updateEvent_ShouldThrowVenueConflictException_WhenVenueHasConflict() {

        UpdateEventRequestDto request =
                createUpdateRequest();

        LocalDateTime newStartTime =
                request.getEventDateTime();

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(eventRepository.existsVenueConflictExcludingEvent(
                eq(request.getVenue()),
                eq(newStartTime.minusHours(1)),
                eq(newStartTime
                        .plusHours(request.getDurationInHours())
                        .plusHours(1)),
                eq(eventId)
        )).thenReturn(true);

        assertThrows(
                VenueConflictException.class,
                () -> eventService.updateEvent(
                        eventId,
                        request,
                        organizerId
                )
        );

        verify(eventRepository)
                .existsVenueConflictExcludingEvent(
                        request.getVenue(),
                        newStartTime.minusHours(1),
                        newStartTime
                                .plusHours(request.getDurationInHours())
                                .plusHours(1),
                        eventId
                );

        verifyNoInteractions(objectMapper);
    }

    // =========================================================
    // CANCEL EVENT
    // =========================================================

    @Test
    void cancelEvent_ShouldCancelEvent_WhenRequestIsValid() {

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        eventService.cancelEvent(
                eventId,
                organizerId
        );

        assertEquals(
                EventStatus.CANCELLED,
                event.getStatus()
        );

        verify(eventRepository).findById(eventId);
    }

    @Test
    void cancelEvent_ShouldThrowResourceNotFoundException_WhenEventDoesNotExist() {

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> eventService.cancelEvent(
                        eventId,
                        organizerId
                )
        );

        verify(eventRepository).findById(eventId);
    }

    @Test
    void cancelEvent_ShouldThrowUnauthorizedAccessException_WhenOrganizerDoesNotOwnEvent() {

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        assertThrows(
                UnauthorizedAccessException.class,
                () -> eventService.cancelEvent(
                        eventId,
                        anotherOrganizerId
                )
        );

        assertEquals(
                EventStatus.PUBLISHED,
                event.getStatus()
        );

        verify(eventRepository).findById(eventId);
    }

    @Test
    void cancelEvent_ShouldThrowInvalidEventStateException_WhenEventIsAlreadyCancelled() {

        event.setStatus(EventStatus.CANCELLED);

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        assertThrows(
                InvalidEventStateException.class,
                () -> eventService.cancelEvent(
                        eventId,
                        organizerId
                )
        );

        verify(eventRepository).findById(eventId);
    }

    @Test
    void cancelEvent_ShouldThrowInvalidEventStateException_WhenEventIsCompleted() {

        event.setStatus(EventStatus.COMPLETED);

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        assertThrows(
                InvalidEventStateException.class,
                () -> eventService.cancelEvent(
                        eventId,
                        organizerId
                )
        );

        verify(eventRepository).findById(eventId);
    }

    // =========================================================
    // HELPER
    // =========================================================

    private UpdateEventRequestDto createUpdateRequest() {

        return UpdateEventRequestDto.builder()
                .title("Updated Tech Conference")
                .description("Updated description")
                .venue("Conference Hall A")
                .eventDateTime(eventDateTime.plusDays(5))
                .durationInHours(5)
                .build();
    }
}