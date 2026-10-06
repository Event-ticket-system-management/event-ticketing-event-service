package com.eventticketing.eventservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventRequestDto {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotBlank(message = "Venue is required")
    private String venue;

    @NotNull(message = "Event date and time is required")
    @Future(message = "Event date must be in the future")
    private LocalDateTime eventDateTime;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 hour")
    @Max(value = 24, message = "Duration cannot exceed 24 hours")
    private Integer durationInHours;
}