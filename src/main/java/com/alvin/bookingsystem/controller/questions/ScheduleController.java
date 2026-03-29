package com.alvin.bookingsystem.controller.questions;

import com.alvin.bookingsystem.dto.request.AddToWaitlistRequest;
import com.alvin.bookingsystem.dto.request.BookClassRequest;
import com.alvin.bookingsystem.dto.response.*;
import com.alvin.bookingsystem.service.ScheduleService;
import com.alvin.bookingsystem.util.ApiResponseUtil;
import com.alvin.bookingsystem.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedules")
@Tag(name = "Question Operations - Schedule Module", description = "Question Operations - Schedule Module APIs (class scheduling and booking)")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping("/countries/{countryId}")
    @Operation(summary = "Get available class schedules by country", description = "Returns list of available class schedules for a specific country")
    public ResponseEntity<ApiResponse> getAvailableSchedulesByCountry(
            @PathVariable Long countryId,
            HttpServletRequest httpServletRequest) {
        List<ClassScheduleResponse> response = scheduleService.getAvailableSchedulesByCountry(countryId);
        return ResponseEntity.ok(ApiResponseUtil.success(response, "Class schedules retrieved successfully", httpServletRequest));
    }

    @PostMapping("/book")
    @Operation(summary = "Book a class", description = "Book a class using user's package. Credits are deducted immediately.")
    public ResponseEntity<ApiResponse> bookClass(
            @Valid @RequestBody BookClassRequest request,
            HttpServletRequest httpServletRequest) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseUtil.error("User not authenticated", HttpStatus.UNAUTHORIZED, httpServletRequest));
        }

        BookClassResponse response = scheduleService.bookClass(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseUtil.created(response, "Class booked successfully", httpServletRequest));
    }

    @PostMapping("/bookings/{bookingId}/cancel")
    @Operation(summary = "Cancel a booking", description = "Cancel a booking. Credits are refunded if cancelled 4+ hours before class start.")
    public ResponseEntity<ApiResponse> cancelBooking(
            @PathVariable Long bookingId,
            HttpServletRequest httpServletRequest) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseUtil.error("User not authenticated", HttpStatus.UNAUTHORIZED, httpServletRequest));
        }

        scheduleService.cancelBooking(userId, bookingId);
        return ResponseEntity.ok(ApiResponseUtil.noContent("Booking cancelled successfully", httpServletRequest));
    }

    @PostMapping("/waitlist")
    @Operation(summary = "Add to waitlist", description = "Add to waitlist when class is full. Required credits are deducted from the package immediately (same as booking); refunded if still waiting after the class ends.")
    public ResponseEntity<ApiResponse> addToWaitlist(
            @Valid @RequestBody AddToWaitlistRequest request,
            HttpServletRequest httpServletRequest) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseUtil.error("User not authenticated", HttpStatus.UNAUTHORIZED, httpServletRequest));
        }

        WaitlistResponse response = scheduleService.addToWaitlist(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseUtil.created(response, "Added to waitlist successfully", httpServletRequest));
    }

    @PostMapping("/bookings/{bookingId}/check-in")
    @Operation(summary = "Check in to class", description = "Check in to a booked class. Available 15 minutes before class starts.")
    public ResponseEntity<ApiResponse> checkIn(
            @PathVariable Long bookingId,
            HttpServletRequest httpServletRequest) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseUtil.error("User not authenticated", HttpStatus.UNAUTHORIZED, httpServletRequest));
        }

        scheduleService.checkIn(userId, bookingId);
        return ResponseEntity.ok(ApiResponseUtil.noContent("Checked in successfully", httpServletRequest));
    }

    @GetMapping("/bookings")
    @Operation(summary = "Get user's bookings", description = "Returns list of user's active bookings")
    public ResponseEntity<ApiResponse> getUserBookings(HttpServletRequest httpServletRequest) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseUtil.error("User not authenticated", HttpStatus.UNAUTHORIZED, httpServletRequest));
        }

        List<BookingResponse> response = scheduleService.getUserBookings(userId);
        return ResponseEntity.ok(ApiResponseUtil.success(response, "Bookings retrieved successfully", httpServletRequest));
    }

    @GetMapping("/waitlists")
    @Operation(summary = "Get user's waitlists", description = "Returns list of user's active waitlist entries")
    public ResponseEntity<ApiResponse> getUserWaitlists(HttpServletRequest httpServletRequest) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseUtil.error("User not authenticated", HttpStatus.UNAUTHORIZED, httpServletRequest));
        }

        List<WaitlistResponse> response = scheduleService.getUserWaitlists(userId);
        return ResponseEntity.ok(ApiResponseUtil.success(response, "Waitlists retrieved successfully", httpServletRequest));
    }
}
