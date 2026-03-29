package com.alvin.bookingsystem.service;

import com.alvin.bookingsystem.dto.request.BookingFilter;
import com.alvin.bookingsystem.dto.request.BookingRequest;
import com.alvin.bookingsystem.dto.response.BookingResponse;

public interface BookingService extends BaseService<BookingRequest, BookingResponse, BookingFilter> {
}
