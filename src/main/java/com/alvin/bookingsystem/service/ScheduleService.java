package com.alvin.bookingsystem.service;

import com.alvin.bookingsystem.dto.request.AddToWaitlistRequest;
import com.alvin.bookingsystem.dto.request.BookClassRequest;
import com.alvin.bookingsystem.dto.response.BookClassResponse;
import com.alvin.bookingsystem.dto.response.BookingResponse;
import com.alvin.bookingsystem.dto.response.ClassScheduleResponse;
import com.alvin.bookingsystem.dto.response.WaitlistResponse;

import java.util.List;

public interface ScheduleService {
    List<ClassScheduleResponse> getAvailableSchedulesByCountry(Long countryId);
    BookClassResponse bookClass(Long userId, BookClassRequest request);
    void cancelBooking(Long userId, Long bookingId);
    WaitlistResponse addToWaitlist(Long userId, AddToWaitlistRequest request);
    void checkIn(Long userId, Long bookingId);
    List<BookingResponse> getUserBookings(Long userId);
    List<WaitlistResponse> getUserWaitlists(Long userId);
}
