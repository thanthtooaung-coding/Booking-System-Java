package com.alvin.bookingsystem.dto.request;

public record ClassDefinitionFilter(
        String name,
        Long countryId,
        Boolean active
) {}
