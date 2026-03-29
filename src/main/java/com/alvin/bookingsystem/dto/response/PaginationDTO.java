package com.alvin.bookingsystem.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record PaginationDTO<T>(
        List<T> content,
        int totalItems,
        int totalPages,
        int currentPage,
        int pageSize
) {}
