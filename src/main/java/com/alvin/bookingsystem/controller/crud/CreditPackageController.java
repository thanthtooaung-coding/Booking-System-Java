package com.alvin.bookingsystem.controller.crud;

import com.alvin.bookingsystem.dto.request.CreditPackageFilter;
import com.alvin.bookingsystem.dto.request.CreditPackageRequest;
import com.alvin.bookingsystem.dto.request.PageAndFilterDTO;
import com.alvin.bookingsystem.dto.response.ApiResponse;
import com.alvin.bookingsystem.dto.response.CreditPackageResponse;
import com.alvin.bookingsystem.dto.response.PaginationDTO;
import com.alvin.bookingsystem.service.CreditPackageService;
import com.alvin.bookingsystem.util.ApiResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crud/credit-packages")
@io.swagger.v3.oas.annotations.tags.Tag(name = "CRUD Operations - Credit Packages", description = "CRUD operations for CreditPackage entity")
public class CreditPackageController {

    private final CreditPackageService creditPackageService;

    @PostMapping
    public ResponseEntity<ApiResponse> create(@Valid @RequestBody CreditPackageRequest request, HttpServletRequest httpServletRequest) {
        CreditPackageResponse response = creditPackageService.create(request);
        ApiResponse apiResponse = ApiResponseUtil.created(response, "Credit package created successfully", httpServletRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> findById(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        CreditPackageResponse response = creditPackageService.findById(id);
        ApiResponse apiResponse = ApiResponseUtil.success(response, "Credit package retrieved successfully", httpServletRequest);
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> update(@PathVariable Long id, @Valid @RequestBody CreditPackageRequest request, HttpServletRequest httpServletRequest) {
        CreditPackageResponse response = creditPackageService.update(id, request);
        ApiResponse apiResponse = ApiResponseUtil.success(response, "Credit package updated successfully", httpServletRequest);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        creditPackageService.delete(id);
        ApiResponse apiResponse = ApiResponseUtil.success(null, "Credit package deleted successfully", httpServletRequest);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse> getAll(@RequestBody PageAndFilterDTO<CreditPackageFilter> pageAndFilterDTO, HttpServletRequest httpServletRequest) {
        PaginationDTO<CreditPackageResponse> response = creditPackageService.getAll(pageAndFilterDTO);
        ApiResponse apiResponse = ApiResponseUtil.paginated(response, "Credit packages retrieved successfully", httpServletRequest);
        return ResponseEntity.ok(apiResponse);
    }
}
