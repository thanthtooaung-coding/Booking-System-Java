package com.alvin.bookingsystem.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BookingResponse(
        Long id,
        Long userId,
        String userEmail,
        Long classScheduleId,
        String className,
        LocalDateTime classDateTime,
        Long userPackageId,
        String packageName,
        Integer creditsUsed,
        String status,
        LocalDateTime bookedAt,
        LocalDateTime cancelledAt,
        LocalDateTime checkedInAt,
        Boolean creditRefunded,
        MasterData masterData
) {}
