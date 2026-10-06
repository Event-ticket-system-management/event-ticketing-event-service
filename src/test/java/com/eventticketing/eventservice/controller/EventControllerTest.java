package com.eventticketing.eventservice.controller;

import com.eventticketing.eventservice.dto.request.CreateEventRequestDto;
import com.eventticketing.eventservice.dto.request.UpdateEventRequestDto;
import com.eventticketing.eventservice.dto.response.EventResponseDto;
import com.eventticketing.eventservice.dto.response.paginate.EventPaginateResponseDto;
import com.eventticketing.eventservice.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    @Mock
    private EventService eventService;

    @InjectMocks
    private EventController eventController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private UUID eventId;
    private UUID organizerId;

    private LocalDateTime eventDateTime;

    private EventResponseDto eventResponseDto;

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders
                .standaloneSetup(eventController)
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());


        eventId = UUID.randomUUID();
        organizerId = UUID.randomUUID();

        eventDateTime = LocalDateTime.now()
                .plusDays(10)
                .withNano(0);

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

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createEvent_ShouldReturnCreated_WhenRequestIsValid()
            throws Exception {

        setAuthenticatedUser(organizerId);

        CreateEventRequestDto request =
                CreateEventRequestDto.builder()
                        .title("Tech Conference 2026")
                        .description("Annual technology conference")
                        .venue("Main Hall")
                        .eventDateTime(eventDateTime)
                        .totalTickets(500)
                        .ticketPrice(new BigDecimal("2500.00"))
                        .durationInHours(4)
                        .build();

        when(eventService.createEvent(
                any(CreateEventRequestDto.class),
                eq(organizerId)
        )).thenReturn(eventResponseDto);

        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isCreated())

                .andExpect(jsonPath("$.id")
                        .value(eventId.toString()))

                .andExpect(jsonPath("$.title")
                        .value("Tech Conference 2026"))

                .andExpect(jsonPath("$.venue")
                        .value("Main Hall"))

                .andExpect(jsonPath("$.totalTickets")
                        .value(500))

                .andExpect(jsonPath("$.availableTickets")
                        .value(500))

                .andExpect(jsonPath("$.ticketPrice")
                        .value(2500.00))

                .andExpect(jsonPath("$.organizerId")
                        .value(organizerId.toString()));

        verify(eventService).createEvent(
                any(CreateEventRequestDto.class),
                eq(organizerId)
        );
    }

    @Test
    void createEvent_ShouldReturnBadRequest_WhenTitleIsBlank()
            throws Exception {

        setAuthenticatedUser(organizerId);

        CreateEventRequestDto request =
                CreateEventRequestDto.builder()
                        .title("")
                        .description("Annual technology conference")
                        .venue("Main Hall")
                        .eventDateTime(eventDateTime)
                        .totalTickets(500)
                        .ticketPrice(new BigDecimal("2500.00"))
                        .durationInHours(4)
                        .build();

        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest());

        verifyNoInteractions(eventService);
    }

    @Test
    void createEvent_ShouldReturnBadRequest_WhenEventDateIsInPast()
            throws Exception {

        setAuthenticatedUser(organizerId);

        CreateEventRequestDto request =
                CreateEventRequestDto.builder()
                        .title("Tech Conference")
                        .description("Test event")
                        .venue("Main Hall")
                        .eventDateTime(LocalDateTime.now().minusDays(1))
                        .totalTickets(500)
                        .ticketPrice(new BigDecimal("2500.00"))
                        .durationInHours(4)
                        .build();

        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest());

        verifyNoInteractions(eventService);
    }

    @Test
    void getEventById_ShouldReturnOk_WhenEventExists()
            throws Exception {

        when(eventService.getEventById(eventId))
                .thenReturn(eventResponseDto);

        mockMvc.perform(
                        get("/api/v1/events/{id}", eventId)
                )

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.id")
                        .value(eventId.toString()))

                .andExpect(jsonPath("$.title")
                        .value("Tech Conference 2026"))

                .andExpect(jsonPath("$.description")
                        .value("Annual technology conference"))

                .andExpect(jsonPath("$.venue")
                        .value("Main Hall"))

                .andExpect(jsonPath("$.totalTickets")
                        .value(500))

                .andExpect(jsonPath("$.organizerId")
                        .value(organizerId.toString()));

        verify(eventService).getEventById(eventId);
    }

    @Test
    void getAllUpcomingEvents_ShouldReturnOk_WithPaginatedEvents()
            throws Exception {

        EventPaginateResponseDto paginateResponse =
                EventPaginateResponseDto.builder()
                        .dataList(List.of(eventResponseDto))
                        .dataCount(1)
                        .build();

        when(eventService.getAllUpComingEvents(0, 10))
                .thenReturn(paginateResponse);

        mockMvc.perform(
                        get("/api/v1/events")
                                .param("page", "0")
                                .param("size", "10")
                )

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.dataCount")
                        .value(1))

                .andExpect(jsonPath("$.dataList")
                        .isArray())

                .andExpect(jsonPath("$.dataList.length()")
                        .value(1))

                .andExpect(jsonPath("$.dataList[0].id")
                        .value(eventId.toString()))

                .andExpect(jsonPath("$.dataList[0].title")
                        .value("Tech Conference 2026"))

                .andExpect(jsonPath("$.dataList[0].venue")
                        .value("Main Hall"));

        verify(eventService)
                .getAllUpComingEvents(0, 10);
    }

    @Test
    void updateEvent_ShouldReturnOk_WhenRequestIsValid()
            throws Exception {

        setAuthenticatedUser(organizerId);

        LocalDateTime updatedDate =
                eventDateTime.plusDays(5);

        UpdateEventRequestDto request =
                UpdateEventRequestDto.builder()
                        .title("Updated Tech Conference")
                        .description("Updated description")
                        .venue("Conference Hall A")
                        .eventDateTime(updatedDate)
                        .durationInHours(5)
                        .build();

        EventResponseDto updatedResponse =
                EventResponseDto.builder()
                        .id(eventId)
                        .title("Updated Tech Conference")
                        .description("Updated description")
                        .venue("Conference Hall A")
                        .eventDateTime(updatedDate)
                        .totalTickets(500)
                        .availableTickets(500)
                        .ticketPrice(new BigDecimal("2500.00"))
                        .organizerId(organizerId)
                        .build();

        when(eventService.updateEvent(
                eq(eventId),
                any(UpdateEventRequestDto.class),
                eq(organizerId)
        )).thenReturn(updatedResponse);

        mockMvc.perform(
                        put("/api/v1/events/{id}", eventId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.id")
                        .value(eventId.toString()))

                .andExpect(jsonPath("$.title")
                        .value("Updated Tech Conference"))

                .andExpect(jsonPath("$.description")
                        .value("Updated description"))

                .andExpect(jsonPath("$.venue")
                        .value("Conference Hall A"));

        verify(eventService).updateEvent(
                eq(eventId),
                any(UpdateEventRequestDto.class),
                eq(organizerId)
        );
    }

    @Test
    void updateEvent_ShouldReturnBadRequest_WhenTitleIsBlank()
            throws Exception {

        setAuthenticatedUser(organizerId);

        UpdateEventRequestDto request =
                UpdateEventRequestDto.builder()
                        .title("")
                        .description("Updated description")
                        .venue("Conference Hall A")
                        .eventDateTime(eventDateTime.plusDays(5))
                        .durationInHours(5)
                        .build();

        mockMvc.perform(
                        put("/api/v1/events/{id}", eventId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )

                .andExpect(status().isBadRequest());

        verifyNoInteractions(eventService);
    }

    @Test
    void cancelEvent_ShouldReturnNoContent_WhenRequestIsValid()
            throws Exception {

        setAuthenticatedUser(organizerId);

        doNothing()
                .when(eventService)
                .cancelEvent(eventId, organizerId);

        mockMvc.perform(
                        delete("/api/v1/events/{id}", eventId)
                )

                .andExpect(status().isNoContent());

        verify(eventService)
                .cancelEvent(eventId, organizerId);
    }

    private void setAuthenticatedUser(UUID userId) {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of()
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);
    }
}