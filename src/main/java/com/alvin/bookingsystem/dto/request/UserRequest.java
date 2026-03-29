package com.alvin.bookingsystem.dto.request;

public record UserRequest(
        String email,
        String password,
        String firstName,
        String lastName,
        String phone
) {}
