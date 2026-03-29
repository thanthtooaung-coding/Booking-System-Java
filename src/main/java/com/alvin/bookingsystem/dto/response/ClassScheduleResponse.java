package com.alvin.bookingsystem.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ClassScheduleResponse(
        Long id,
        Long classDefinitionId,
        String className,
        String classDescription,
        Integer requiredCredits,
        Integer durationMinutes,
        String instructorName,
        Long countryId,
        String countryName,
        String countryCode,
        LocalDateTime classDateTime,
        Integer maxSlots,
        Integer bookedSlots,
        Integer availableSlots,
        String status
) {}
