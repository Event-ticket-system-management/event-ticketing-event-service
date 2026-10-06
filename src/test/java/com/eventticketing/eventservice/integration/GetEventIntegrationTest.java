package com.eventticketing.eventservice.integration;

import com.eventticketing.eventservice.entity.Event;
import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GetEventIntegrationTest
        extends AbstractEventIntegrationTest {

    @Test
    void getEventById_ShouldReturnEvent_WhenEventExists()
            throws Exception {

        Event savedEvent = saveEvent(
                organizerId,
                "Tech Conference",
                "Main Hall",
                eventDateTime,
                eventDateTime.plusHours(4)
        );

        mockMvc.perform(
                        get(
                                "/api/v1/events/{id}",
                                savedEvent.getId()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(savedEvent.getId().toString()))
                .andExpect(jsonPath("$.title")
                        .value("Tech Conference"))
                .andExpect(jsonPath("$.venue")
                        .value("Main Hall"))
                .andExpect(jsonPath("$.organizerId")
                        .value(organizerId.toString()));
    }

    @Test
    void getEventById_ShouldReturnNotFound_WhenEventDoesNotExist()
            throws Exception {

        UUID unknownEventId =
                UUID.randomUUID();

        mockMvc.perform(
                        get(
                                "/api/v1/events/{id}",
                                unknownEventId
                        )
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllUpcomingEvents_ShouldReturnPaginatedEvents()
            throws Exception {

        saveEvent(
                organizerId,
                "Event One",
                "Hall A",
                eventDateTime,
                eventDateTime.plusHours(2)
        );

        saveEvent(
                organizerId,
                "Event Two",
                "Hall B",
                eventDateTime.plusDays(1),
                eventDateTime.plusDays(1)
                        .plusHours(3)
        );

        mockMvc.perform(
                        get("/api/v1/events")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataCount")
                        .value(2))
                .andExpect(jsonPath("$.dataList")
                        .isArray())
                .andExpect(jsonPath("$.dataList.length()")
                        .value(2));
    }

    @Test
    void getAllUpcomingEvents_ShouldReturnEmptyList_WhenNoEventsExist()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/events")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataCount")
                        .value(0))
                .andExpect(jsonPath("$.dataList")
                        .isEmpty());
    }
}