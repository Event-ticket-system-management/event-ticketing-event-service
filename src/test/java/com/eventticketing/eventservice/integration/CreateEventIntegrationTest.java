package com.eventticketing.eventservice.integration;

import com.eventticketing.eventservice.entity.Event;
import com.eventticketing.eventservice.enums.EventStatus;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CreateEventIntegrationTest
        extends AbstractEventIntegrationTest {

    @Test
    void createEvent_ShouldCreateAndPersistEvent()
            throws Exception {

        String requestBody = """
                {
                    "title": "Tech Conference 2026",
                    "description": "Annual technology conference",
                    "venue": "Main Hall",
                    "eventDateTime": "%s",
                    "totalTickets": 500,
                    "ticketPrice": 2500.00,
                    "durationInHours": 4
                }
                """.formatted(eventDateTime);

        mockMvc.perform(
                        post("/api/v1/events")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title")
                        .value("Tech Conference 2026"))
                .andExpect(jsonPath("$.venue")
                        .value("Main Hall"))
                .andExpect(jsonPath("$.totalTickets")
                        .value(500))
                .andExpect(jsonPath("$.availableTickets")
                        .value(500))
                .andExpect(jsonPath("$.organizerId")
                        .value(organizerId.toString()));

        List<Event> events =
                eventRepository.findAll();

        assertThat(events)
                .hasSize(1);

        Event savedEvent =
                events.getFirst();

        assertThat(savedEvent.getTitle())
                .isEqualTo("Tech Conference 2026");

        assertThat(savedEvent.getOrganizerId())
                .isEqualTo(organizerId);

        assertThat(savedEvent.getTotalTickets())
                .isEqualTo(500);

        assertThat(savedEvent.getAvailableTickets())
                .isEqualTo(500);

        assertThat(savedEvent.getStatus())
                .isEqualTo(EventStatus.PUBLISHED);

        assertThat(savedEvent.getEndDateTime())
                .isEqualTo(
                        eventDateTime.plusHours(4)
                );
    }

    @Test
    void createEvent_ShouldReturnConflict_WhenVenueIsAlreadyBooked()
            throws Exception {

        saveEvent(
                organizerId,
                "Existing Event",
                "Main Hall",
                eventDateTime,
                eventDateTime.plusHours(4)
        );

        String requestBody = getRequestBody();

        mockMvc.perform(
                        post("/api/v1/events")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isConflict());

        assertThat(eventRepository.count())
                .isEqualTo(1);
    }

    private @NonNull String getRequestBody() {
        LocalDateTime conflictingStart =
                eventDateTime.plusHours(2);

        return """
                {
                    "title": "Another Event",
                    "description": "Conflicting event",
                    "venue": "Main Hall",
                    "eventDateTime": "%s",
                    "totalTickets": 100,
                    "ticketPrice": 1000.00,
                    "durationInHours": 2
                }
                """.formatted(conflictingStart);
    }

    @Test
    void createEvent_ShouldReturnBadRequest_WhenRequestIsInvalid()
            throws Exception {

        String requestBody = """
                {
                    "title": "",
                    "description": "Invalid event",
                    "venue": "",
                    "eventDateTime": "%s",
                    "totalTickets": 0,
                    "ticketPrice": -100,
                    "durationInHours": 0
                }
                """.formatted(
                LocalDateTime.now()
                        .minusDays(1)
                        .withNano(0)
        );

        mockMvc.perform(
                        post("/api/v1/events")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());

        assertThat(eventRepository.count())
                .isZero();
    }
}