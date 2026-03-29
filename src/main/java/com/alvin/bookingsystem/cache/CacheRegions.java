package com.alvin.bookingsystem.cache;

/**
 * Redis / Spring Cache region names for CRUD response caching ({@code findById} / create / update / delete).
 */
public final class CacheRegions {

    private CacheRegions() {}

    public static final String BOOKINGS = "bookings";
    public static final String CLASS_DEFINITIONS = "class-definitions";
    public static final String CLASS_SCHEDULES = "class-schedules";
    public static final String COUNTRIES = "countries";
    public static final String CREDIT_PACKAGES = "credit-packages";
    public static final String USER_PACKAGES = "user-packages";
    public static final String USERS = "users";
    public static final String WAITLISTS = "waitlists";
}
