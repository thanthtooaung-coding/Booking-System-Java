package com.alvin.bookingsystem.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record WaitlistResponse(
        Long id,
        Long userId,
        String userEmail,
        Long classScheduleId,
        String className,
        LocalDateTime classDateTime,
        Long userPackageId,
        String packageName,
        Integer creditsReserved,
        Integer position,
        String status,
        LocalDateTime joinedAt,
        LocalDateTime promotedAt,
        LocalDateTime refundedAt,
        MasterData masterData
) {}
