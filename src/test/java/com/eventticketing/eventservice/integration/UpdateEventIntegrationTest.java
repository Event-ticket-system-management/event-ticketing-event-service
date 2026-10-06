package com.eventticketing.eventservice.integration;

import com.eventticketing.eventservice.entity.Event;
import com.eventticketing.eventservice.enums.EventStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UpdateEventIntegrationTest
        extends AbstractEventIntegrationTest {

    @Test
    void updateEvent_ShouldUpdatePersistedEvent()
            throws Exception {

        Event savedEvent = saveEvent(
                organizerId,
                "Old Event",
                "Old Hall",
                eventDateTime,
                eventDateTime.plusHours(4)
        );

        LocalDateTime newStart =
                eventDateTime.plusDays(5);

        String requestBody = """
                {
                    "title": "Updated Event",
                    "description": "Updated description",
                    "venue": "New Hall",
                    "eventDateTime": "%s",
                    "durationInHours": 5
                }
                """.formatted(newStart);

        mockMvc.perform(
                        put(
                                "/api/v1/events/{id}",
                                savedEvent.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title")
                        .value("Updated Event"))
                .andExpect(jsonPath("$.venue")
                        .value("New Hall"));

        Event updatedEvent =
                eventRepository
                        .findById(savedEvent.getId())
                        .orElseThrow();

        assertThat(updatedEvent.getTitle())
                .isEqualTo("Updated Event");

        assertThat(updatedEvent.getDescription())
                .isEqualTo("Updated description");

        assertThat(updatedEvent.getVenue())
                .isEqualTo("New Hall");

        assertThat(updatedEvent.getEventDateTime())
                .isEqualTo(newStart);

        assertThat(updatedEvent.getEndDateTime())
                .isEqualTo(newStart.plusHours(5));
    }

    @Test
    void updateEvent_ShouldReturnForbidden_WhenOrganizerDoesNotOwnEvent()
            throws Exception {

        UUID actualOwner =
                UUID.randomUUID();

        Event savedEvent = saveEvent(
                actualOwner,
                "Protected Event",
                "Main Hall",
                eventDateTime,
                eventDateTime.plusHours(4)
        );

        String requestBody = """
                {
                    "title": "Illegal Update",
                    "description": "Should not update",
                    "venue": "Another Hall",
                    "eventDateTime": "%s",
                    "durationInHours": 4
                }
                """.formatted(
                eventDateTime.plusDays(2)
        );

        mockMvc.perform(
                        put(
                                "/api/v1/events/{id}",
                                savedEvent.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isForbidden());

        Event unchangedEvent =
                eventRepository
                        .findById(savedEvent.getId())
                        .orElseThrow();

        assertThat(unchangedEvent.getTitle())
                .isEqualTo("Protected Event");
    }

    @Test
    void updateEvent_ShouldReturnConflict_WhenVenueHasConflict()
            throws Exception {

        Event selectedEvent = saveEvent(
                organizerId,
                "My Event",
                "Hall A",
                eventDateTime,
                eventDateTime.plusHours(2)
        );

        LocalDateTime occupiedStart =
                eventDateTime.plusDays(5);

        saveEvent(
                UUID.randomUUID(),
                "Other Event",
                "Main Hall",
                occupiedStart,
                occupiedStart.plusHours(4)
        );

        String requestBody = """
                {
                    "title": "Updated Event",
                    "description": "Updated description",
                    "venue": "Main Hall",
                    "eventDateTime": "%s",
                    "durationInHours": 2
                }
                """.formatted(
                occupiedStart.plusHours(1)
        );

        mockMvc.perform(
                        put(
                                "/api/v1/events/{id}",
                                selectedEvent.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isConflict());
    }

    @Test
    void updateEvent_ShouldFail_WhenEventIsCancelled()
            throws Exception {

        Event savedEvent = saveEvent(
                organizerId,
                "Cancelled Event",
                "Main Hall",
                eventDateTime,
                eventDateTime.plusHours(4)
        );

        savedEvent.setStatus(
                EventStatus.CANCELLED
        );

        eventRepository.save(savedEvent);

        String requestBody = """
                {
                    "title": "Updated Event",
                    "description": "Should not update",
                    "venue": "New Hall",
                    "eventDateTime": "%s",
                    "durationInHours": 4
                }
                """.formatted(
                eventDateTime.plusDays(2)
        );

        mockMvc.perform(
                        put(
                                "/api/v1/events/{id}",
                                savedEvent.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }
}