package com.alvin.bookingsystem.dto.response;

import lombok.Builder;

@Builder
public record PaginationApiMetaResponse(
        String endpoint,
        String method,
        int totalItems,
        int totalPages,
        int currentPage
) {}
