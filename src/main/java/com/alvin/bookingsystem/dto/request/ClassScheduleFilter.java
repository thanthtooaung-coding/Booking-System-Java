package com.alvin.bookingsystem.dto.request;

import java.time.LocalDateTime;

public record ClassScheduleFilter(
        Long classDefinitionId,
        Long countryId,
        LocalDateTime classDateTime,
        String status
) {}
