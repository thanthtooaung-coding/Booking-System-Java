package com.alvin.bookingsystem.util;

import com.alvin.bookingsystem.dto.response.ApiMetaResponse;
import com.alvin.bookingsystem.dto.response.ApiResponse;
import com.alvin.bookingsystem.dto.response.PaginationApiMetaResponse;
import com.alvin.bookingsystem.dto.response.PaginationDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

public class ApiResponseUtil {

    public static ApiResponse success(Object data, String message, HttpServletRequest request) {
        ApiMetaResponse meta = ApiMetaResponse.builder()
                .endpoint(request.getRequestURI())
                .method(request.getMethod())
                .build();

        return ApiResponse.builder()
                .success(1)
                .code(HttpStatus.OK.value())
                .meta(meta)
                .data(data)
                .message(message)
                .build();
    }

    public static ApiResponse created(Object data, String message, HttpServletRequest request) {
        ApiMetaResponse meta = ApiMetaResponse.builder()
                .endpoint(request.getRequestURI())
                .method(request.getMethod())
                .build();

        return ApiResponse.builder()
                .success(1)
                .code(HttpStatus.CREATED.value())
                .meta(meta)
                .data(data)
                .message(message)
                .build();
    }

    public static ApiResponse noContent(String message, HttpServletRequest request) {
        ApiMetaResponse meta = ApiMetaResponse.builder()
                .endpoint(request.getRequestURI())
                .method(request.getMethod())
                .build();

        return ApiResponse.builder()
                .success(1)
                .code(HttpStatus.NO_CONTENT.value())
                .meta(meta)
                .data(null)
                .message(message)
                .build();
    }

    public static <T> ApiResponse paginated(PaginationDTO<T> pagination, String message, HttpServletRequest request) {
        PaginationApiMetaResponse meta = PaginationApiMetaResponse.builder()
                .endpoint(request.getRequestURI())
                .method(request.getMethod())
                .totalItems(pagination.totalItems())
                .totalPages(pagination.totalPages())
                .currentPage(pagination.currentPage())
                .build();

        return ApiResponse.builder()
                .success(1)
                .code(HttpStatus.OK.value())
                .meta(meta)
                .data(pagination)
                .message(message)
                .build();
    }

    public static ApiResponse error(String message, HttpStatus status, HttpServletRequest request) {
        ApiMetaResponse meta = ApiMetaResponse.builder()
                .endpoint(request.getRequestURI())
                .method(request.getMethod())
                .build();

        return ApiResponse.builder()
                .success(0)
                .code(status.value())
                .meta(meta)
                .data(null)
                .message(message)
                .build();
    }

    public static ApiResponse error(String message, HttpStatus status, Object data, HttpServletRequest request) {
        ApiMetaResponse meta = ApiMetaResponse.builder()
                .endpoint(request.getRequestURI())
                .method(request.getMethod())
                .build();

        return ApiResponse.builder()
                .success(0)
                .code(status.value())
                .meta(meta)
                .data(data)
                .message(message)
                .build();
    }
}
