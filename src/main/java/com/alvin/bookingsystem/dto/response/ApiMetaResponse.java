package com.alvin.bookingsystem.dto.response;

import lombok.Builder;

@Builder
public record ApiMetaResponse(
        String endpoint,
        String method
) {}
