package com.eventticketing.eventservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidEventStateException extends RuntimeException {
    public InvalidEventStateException(String message) {
        super(message);
    }
}