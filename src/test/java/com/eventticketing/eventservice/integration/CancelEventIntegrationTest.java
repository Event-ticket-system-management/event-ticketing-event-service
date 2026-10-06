package com.eventticketing.eventservice.integration;

import com.eventticketing.eventservice.entity.Event;
import com.eventticketing.eventservice.enums.EventStatus;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CancelEventIntegrationTest
        extends AbstractEventIntegrationTest {

    @Test
    void cancelEvent_ShouldChangeStatusToCancelled()
            throws Exception {

        Event savedEvent = saveEvent(
                organizerId,
                "Event To Cancel",
                "Main Hall",
                eventDateTime,
                eventDateTime.plusHours(4)
        );

        mockMvc.perform(
                        delete(
                                "/api/v1/events/{id}",
                                savedEvent.getId()
                        )
                )
                .andExpect(status().isNoContent());

        Event cancelledEvent =
                eventRepository
                        .findById(savedEvent.getId())
                        .orElseThrow();

        assertThat(cancelledEvent.getStatus())
                .isEqualTo(EventStatus.CANCELLED);

        assertThat(
                eventRepository.existsById(
                        savedEvent.getId()
                )
        ).isTrue();
    }

    @Test
    void cancelEvent_ShouldReturnForbidden_WhenOrganizerDoesNotOwnEvent()
            throws Exception {

        UUID actualOwner =
                UUID.randomUUID();

        Event savedEvent = saveEvent(
                actualOwner,
                "Another Organizer Event",
                "Main Hall",
                eventDateTime,
                eventDateTime.plusHours(4)
        );

        mockMvc.perform(
                        delete(
                                "/api/v1/events/{id}",
                                savedEvent.getId()
                        )
                )
                .andExpect(status().isForbidden());

        Event unchangedEvent =
                eventRepository
                        .findById(savedEvent.getId())
                        .orElseThrow();

        assertThat(unchangedEvent.getStatus())
                .isEqualTo(EventStatus.PUBLISHED);
    }

    @Test
    void cancelEvent_ShouldFail_WhenEventIsAlreadyCancelled()
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

        mockMvc.perform(
                        delete(
                                "/api/v1/events/{id}",
                                savedEvent.getId()
                        )
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelEvent_ShouldFail_WhenEventIsCompleted()
            throws Exception {

        Event savedEvent = saveEvent(
                organizerId,
                "Completed Event",
                "Main Hall",
                eventDateTime,
                eventDateTime.plusHours(4)
        );

        savedEvent.setStatus(
                EventStatus.COMPLETED
        );

        eventRepository.save(savedEvent);

        mockMvc.perform(
                        delete(
                                "/api/v1/events/{id}",
                                savedEvent.getId()
                        )
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelEvent_ShouldReturnNotFound_WhenEventDoesNotExist()
            throws Exception {

        UUID unknownId =
                UUID.randomUUID();

        mockMvc.perform(
                        delete(
                                "/api/v1/events/{id}",
                                unknownId
                        )
                )
                .andExpect(status().isNotFound());
    }
}