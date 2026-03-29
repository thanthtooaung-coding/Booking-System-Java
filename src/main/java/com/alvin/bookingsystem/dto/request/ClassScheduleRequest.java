package com.alvin.bookingsystem.dto.request;

import java.time.LocalDateTime;

public record ClassScheduleRequest(
        Long classDefinitionId,
        Long countryId,
        LocalDateTime classDateTime,
        Integer maxSlots,
        String status
) {}
