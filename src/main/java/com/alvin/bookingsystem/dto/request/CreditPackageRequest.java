package com.alvin.bookingsystem.dto.request;

import java.math.BigDecimal;

public record CreditPackageRequest(
        String name,
        Long countryId,
        Integer credits,
        BigDecimal price,
        Integer validityDays,
        String description,
        Boolean active
) {}
