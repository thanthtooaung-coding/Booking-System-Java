package com.alvin.bookingsystem.dto.request;

public record UserFilter(
        String email,
        String firstName,
        String lastName,
        Boolean emailVerified,
        Boolean active
) {}
