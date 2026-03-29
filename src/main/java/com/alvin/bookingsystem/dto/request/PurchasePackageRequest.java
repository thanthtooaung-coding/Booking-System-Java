package com.alvin.bookingsystem.dto.request;

import jakarta.validation.constraints.NotNull;

public record PurchasePackageRequest(
        @NotNull(message = "Credit package ID cannot be null")
        Long creditPackageId
) {}
