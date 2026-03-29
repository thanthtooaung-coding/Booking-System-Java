package com.alvin.bookingsystem.dto.request;

import java.time.LocalDateTime;

public record WaitlistRequest(
        Long userId,
        Long classScheduleId,
        Long userPackageId,
        Integer creditsReserved,
        Integer position,
        String status,
        LocalDateTime joinedAt
) {}
