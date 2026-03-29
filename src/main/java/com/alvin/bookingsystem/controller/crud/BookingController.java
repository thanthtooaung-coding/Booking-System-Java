package com.alvin.bookingsystem.controller.crud;

import com.alvin.bookingsystem.dto.request.BookingFilter;
import com.alvin.bookingsystem.dto.request.BookingRequest;
import com.alvin.bookingsystem.dto.request.PageAndFilterDTO;
import com.alvin.bookingsystem.dto.response.ApiResponse;
import com.alvin.bookingsystem.dto.response.BookingResponse;
import com.alvin.bookingsystem.dto.response.PaginationDTO;
import com.alvin.bookingsystem.service.BookingService;
import com.alvin.bookingsystem.util.ApiResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crud/bookings")
@io.swagger.v3.oas.annotations.tags.Tag(name = "CRUD Operations - Bookings", description = "CRUD operations for Booking entity")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<ApiResponse> create(@Valid @RequestBody BookingRequest request, HttpServletRequest httpServletRequest) {
        BookingResponse response = bookingService.create(request);
        ApiResponse apiResponse = ApiResponseUtil.created(response, "Booking created successfully", httpServletRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> findById(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        BookingResponse response = bookingService.findById(id);
        ApiResponse apiResponse = ApiResponseUtil.success(response, "Booking retrieved successfully", httpServletRequest);
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> update(@PathVariable Long id, @Valid @RequestBody BookingRequest request, HttpServletRequest httpServletRequest) {
        BookingResponse response = bookingService.update(id, request);
        ApiResponse apiResponse = ApiResponseUtil.success(response, "Booking updated successfully", httpServletRequest);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        bookingService.delete(id);
        ApiResponse apiResponse = ApiResponseUtil.success(null, "Booking deleted successfully", httpServletRequest);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse> getAll(@RequestBody PageAndFilterDTO<BookingFilter> pageAndFilterDTO, HttpServletRequest httpServletRequest) {
        PaginationDTO<BookingResponse> response = bookingService.getAll(pageAndFilterDTO);
        ApiResponse apiResponse = ApiResponseUtil.paginated(response, "Bookings retrieved successfully", httpServletRequest);
        return ResponseEntity.ok(apiResponse);
    }
}
