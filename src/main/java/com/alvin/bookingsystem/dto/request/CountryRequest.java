package com.alvin.bookingsystem.dto.request;

public record CountryRequest(
        String code,
        String name,
        Boolean active
) {}
