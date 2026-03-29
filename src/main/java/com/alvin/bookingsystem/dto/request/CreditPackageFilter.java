package com.alvin.bookingsystem.dto.request;

public record CreditPackageFilter(
        String name,
        Long countryId,
        Boolean active
) {}
