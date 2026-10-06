package com.eventticketing.eventservice.integration;

import com.eventticketing.eventservice.entity.Event;
import com.eventticketing.eventservice.enums.EventStatus;
import com.eventticketing.eventservice.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public abstract class AbstractEventIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected EventRepository eventRepository;

    protected UUID organizerId;
    protected LocalDateTime eventDateTime;

    @Autowired
    protected CacheManager cacheManager;

    @BeforeEach
    void baseSetUp() {

        eventRepository.deleteAll();
        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);

            if (cache != null){
                cache.clear();
            }
        });

        SecurityContextHolder.clearContext();
        organizerId = UUID.randomUUID();

        eventDateTime = LocalDateTime.now()
                .plusDays(10)
                .withNano(0);

        setAuthenticatedOrganizer(organizerId);
    }

    protected Event saveEvent(
            UUID ownerId,
            String title,
            String venue,
            LocalDateTime start,
            LocalDateTime end
    ) {

        Event event = Event.builder()
                .title(title)
                .description("Integration test event")
                .venue(venue)
                .eventDateTime(start)
                .endDateTime(end)
                .totalTickets(500)
                .availableTickets(500)
                .ticketPrice(new BigDecimal("2500.00"))
                .organizerId(ownerId)
                .status(EventStatus.PUBLISHED)
                .build();

        return eventRepository.save(event);
    }

    protected void setAuthenticatedOrganizer(UUID userId) {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_ORGANIZER"
                                )
                        )
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);
    }
}