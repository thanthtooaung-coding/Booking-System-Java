package com.alvin.bookingsystem.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CreditPackageResponse(
        Long id,
        String name,
        Long countryId,
        String countryName,
        Integer credits,
        BigDecimal price,
        Integer validityDays,
        String description,
        Boolean active,
        MasterData masterData
) {}
