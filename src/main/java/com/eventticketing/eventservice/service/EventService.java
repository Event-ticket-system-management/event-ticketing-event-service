package com.eventticketing.eventservice.service;

import com.eventticketing.eventservice.dto.request.CreateEventRequestDto;
import com.eventticketing.eventservice.dto.request.UpdateEventRequestDto;
import com.eventticketing.eventservice.dto.response.EventResponseDto;
import com.eventticketing.eventservice.dto.response.paginate.EventPaginateResponseDto;
import java.util.UUID;

public interface EventService {

    public EventResponseDto createEvent(CreateEventRequestDto request, UUID organizerId);
    public EventResponseDto getEventById(UUID id);
    public EventPaginateResponseDto getAllUpComingEvents(int page, int size);
    public EventResponseDto updateEvent(UUID id, UpdateEventRequestDto request, UUID organizerId);
    public void cancelEvent(UUID id, UUID organizerId);

}
