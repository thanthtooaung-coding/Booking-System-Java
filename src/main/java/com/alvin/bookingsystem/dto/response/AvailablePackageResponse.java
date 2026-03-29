package com.alvin.bookingsystem.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record AvailablePackageResponse(
        Long id,
        String name,
        Long countryId,
        String countryName,
        String countryCode,
        Integer credits,
        BigDecimal price,
        Integer validityDays,
        String description,
        Boolean active
) {}
