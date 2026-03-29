package com.alvin.bookingsystem.controller.crud;

import com.alvin.bookingsystem.dto.request.PageAndFilterDTO;
import com.alvin.bookingsystem.dto.request.WaitlistFilter;
import com.alvin.bookingsystem.dto.request.WaitlistRequest;
import com.alvin.bookingsystem.dto.response.ApiResponse;
import com.alvin.bookingsystem.dto.response.PaginationDTO;
import com.alvin.bookingsystem.dto.response.WaitlistResponse;
import com.alvin.bookingsystem.service.WaitlistService;
import com.alvin.bookingsystem.util.ApiResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crud/waitlists")
@io.swagger.v3.oas.annotations.tags.Tag(name = "CRUD Operations - Waitlists", description = "CRUD operations for Waitlist entity")
public class WaitlistController {

    private final WaitlistService waitlistService;

    @PostMapping
    public ResponseEntity<ApiResponse> create(@Valid @RequestBody WaitlistRequest request, HttpServletRequest httpServletRequest) {
        WaitlistResponse response = waitlistService.create(request);
        ApiResponse apiResponse = ApiResponseUtil.created(response, "Waitlist created successfully", httpServletRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> findById(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        WaitlistResponse response = waitlistService.findById(id);
        ApiResponse apiResponse = ApiResponseUtil.success(response, "Waitlist retrieved successfully", httpServletRequest);
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> update(@PathVariable Long id, @Valid @RequestBody WaitlistRequest request, HttpServletRequest httpServletRequest) {
        WaitlistResponse response = waitlistService.update(id, request);
        ApiResponse apiResponse = ApiResponseUtil.success(response, "Waitlist updated successfully", httpServletRequest);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        waitlistService.delete(id);
        ApiResponse apiResponse = ApiResponseUtil.success(null, "Waitlist deleted successfully", httpServletRequest);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse> getAll(@RequestBody PageAndFilterDTO<WaitlistFilter> pageAndFilterDTO, HttpServletRequest httpServletRequest) {
        PaginationDTO<WaitlistResponse> response = waitlistService.getAll(pageAndFilterDTO);
        ApiResponse apiResponse = ApiResponseUtil.paginated(response, "Waitlists retrieved successfully", httpServletRequest);
        return ResponseEntity.ok(apiResponse);
    }
}
