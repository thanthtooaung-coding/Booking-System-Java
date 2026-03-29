package com.alvin.bookingsystem.dto.request;

public record ClassDefinitionRequest(
        String name,
        Long countryId,
        Integer requiredCredits,
        Integer durationMinutes,
        String description,
        String instructorName,
        Boolean active
) {}
