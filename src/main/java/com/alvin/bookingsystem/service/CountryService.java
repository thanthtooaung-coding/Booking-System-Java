package com.alvin.bookingsystem.service;

import com.alvin.bookingsystem.dto.request.CountryFilter;
import com.alvin.bookingsystem.dto.request.CountryRequest;
import com.alvin.bookingsystem.dto.response.CountryResponse;

public interface CountryService extends BaseService<CountryRequest, CountryResponse, CountryFilter> {
}
