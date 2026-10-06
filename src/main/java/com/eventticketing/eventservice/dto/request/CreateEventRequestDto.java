package com.eventticketing.eventservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventRequestDto {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotBlank(message = "Venue is required")
    private String venue;

    @NotNull(message = "Event date and time is required")
    @Future(message = "Event date must be in the future")
    private LocalDateTime eventDateTime;

    @NotNull(message = "Total tickets is required")
    @Min(value = 1, message = "Total tickets must be at least 1")
    private Integer totalTickets;

    @NotNull(message = "Ticket price is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Ticket price cannot be negative")
    private BigDecimal ticketPrice;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 hour")
    @Max(value = 24, message = "Duration cannot exceed 24 hours")
    private Integer durationInHours;
}