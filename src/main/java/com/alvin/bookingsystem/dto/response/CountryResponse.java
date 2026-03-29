package com.alvin.bookingsystem.dto.response;

import lombok.Builder;

@Builder
public record CountryResponse(
        Long id,
        String code,
        String name,
        Boolean active,
        MasterData masterData
) {}
