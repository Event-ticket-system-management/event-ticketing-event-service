package com.eventticketing.eventservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class VenueConflictException extends RuntimeException {
    public VenueConflictException(String message) {
        super(message);
    }
}
