package com.alvin.bookingsystem.dto.request;

import java.time.LocalDateTime;

public record BookingRequest(
        Long userId,
        Long classScheduleId,
        Long userPackageId,
        Integer creditsUsed,
        String status,
        LocalDateTime bookedAt
) {}
