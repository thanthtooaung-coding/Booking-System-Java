package com.alvin.bookingsystem.dto.response;

import lombok.Builder;

@Builder
public record ClassDefinitionResponse(
        Long id,
        String name,
        Long countryId,
        String countryName,
        Integer requiredCredits,
        Integer durationMinutes,
        String description,
        String instructorName,
        Boolean active,
        MasterData masterData
) {}
