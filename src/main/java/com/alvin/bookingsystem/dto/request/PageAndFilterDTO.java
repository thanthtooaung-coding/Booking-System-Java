package com.alvin.bookingsystem.dto.request;

import lombok.Data;

@Data
public class PageAndFilterDTO<FILTER> {
    private int page = 0;
    private int size = 10;
    private String sortBy;
    private String sortDirection = "ASC";
    private FILTER filter;
}
