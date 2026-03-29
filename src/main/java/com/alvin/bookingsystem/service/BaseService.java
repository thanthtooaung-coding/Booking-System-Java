package com.alvin.bookingsystem.service;

import com.alvin.bookingsystem.dto.request.PageAndFilterDTO;
import com.alvin.bookingsystem.dto.response.PaginationDTO;

public interface BaseService<REQUEST, RESPONSE, FILTER> {
    RESPONSE create(REQUEST request);
    RESPONSE findById(Long id);
    RESPONSE update(Long id, REQUEST request);
    void delete(Long id);
    PaginationDTO<RESPONSE> getAll(PageAndFilterDTO<FILTER> pageAndFilterDTO);
}
