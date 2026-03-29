package com.alvin.bookingsystem.dto.request;

public record LoginRequest(
        String email,
        String password
) {}
