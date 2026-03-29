package com.alvin.bookingsystem.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BookClassResponse(
        Long bookingId,
        Long classScheduleId,
        String className,
        LocalDateTime classDateTime,
        Integer creditsUsed,
        Integer remainingCredits,
        String status,
        LocalDateTime bookedAt
) {}
