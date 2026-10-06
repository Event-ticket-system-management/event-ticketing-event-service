package com.eventticketing.eventservice.mapper;

import com.eventticketing.eventservice.dto.response.EventResponseDto;
import com.eventticketing.eventservice.entity.Event;
import org.springframework.stereotype.Component;

@Component
public class ObjectMapper {

    public EventResponseDto toEventResponse(Event event){
        return EventResponseDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .venue(event.getVenue())
                .eventDateTime(event.getEventDateTime())
                .totalTickets(event.getTotalTickets())
                .availableTickets(event.getAvailableTickets())
                .ticketPrice(event.getTicketPrice())
                .organizerId(event.getOrganizerId())
                .status(event.getStatus())
                .createdAt(event.getCreatedAt())
                .build();

    }

}
