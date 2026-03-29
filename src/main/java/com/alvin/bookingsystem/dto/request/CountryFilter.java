package com.alvin.bookingsystem.dto.request;

public record CountryFilter(
        String code,
        String name,
        Boolean active
) {}
