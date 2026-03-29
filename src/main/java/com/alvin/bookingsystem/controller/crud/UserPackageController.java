package com.alvin.bookingsystem.controller.crud;

import com.alvin.bookingsystem.dto.request.PageAndFilterDTO;
import com.alvin.bookingsystem.dto.request.UserPackageFilter;
import com.alvin.bookingsystem.dto.request.UserPackageRequest;
import com.alvin.bookingsystem.dto.response.ApiResponse;
import com.alvin.bookingsystem.dto.response.PaginationDTO;
import com.alvin.bookingsystem.dto.response.UserPackageResponse;
import com.alvin.bookingsystem.service.UserPackageService;
import com.alvin.bookingsystem.util.ApiResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crud/user-packages")
@io.swagger.v3.oas.annotations.tags.Tag(name = "CRUD Operations - User Packages", description = "CRUD operations for UserPackage entity")
public class UserPackageController {

    private final UserPackageService userPackageService;

    @PostMapping
    public ResponseEntity<ApiResponse> create(@Valid @RequestBody UserPackageRequest request, HttpServletRequest httpServletRequest) {
        UserPackageResponse response = userPackageService.create(request);
        ApiResponse apiResponse = ApiResponseUtil.created(response, "User package created successfully", httpServletRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> findById(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        UserPackageResponse response = userPackageService.findById(id);
        ApiResponse apiResponse = ApiResponseUtil.success(response, "User package retrieved successfully", httpServletRequest);
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> update(@PathVariable Long id, @Valid @RequestBody UserPackageRequest request, HttpServletRequest httpServletRequest) {
        UserPackageResponse response = userPackageService.update(id, request);
        ApiResponse apiResponse = ApiResponseUtil.success(response, "User package updated successfully", httpServletRequest);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        userPackageService.delete(id);
        ApiResponse apiResponse = ApiResponseUtil.success(null, "User package deleted successfully", httpServletRequest);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse> getAll(@RequestBody PageAndFilterDTO<UserPackageFilter> pageAndFilterDTO, HttpServletRequest httpServletRequest) {
        PaginationDTO<UserPackageResponse> response = userPackageService.getAll(pageAndFilterDTO);
        ApiResponse apiResponse = ApiResponseUtil.paginated(response, "User packages retrieved successfully", httpServletRequest);
        return ResponseEntity.ok(apiResponse);
    }
}
