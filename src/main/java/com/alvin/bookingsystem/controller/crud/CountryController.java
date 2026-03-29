package com.alvin.bookingsystem.controller.crud;

import com.alvin.bookingsystem.dto.request.CountryFilter;
import com.alvin.bookingsystem.dto.request.CountryRequest;
import com.alvin.bookingsystem.dto.request.PageAndFilterDTO;
import com.alvin.bookingsystem.dto.response.ApiResponse;
import com.alvin.bookingsystem.dto.response.CountryResponse;
import com.alvin.bookingsystem.dto.response.PaginationDTO;
import com.alvin.bookingsystem.service.CountryService;
import com.alvin.bookingsystem.util.ApiResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crud/countries")
@io.swagger.v3.oas.annotations.tags.Tag(name = "CRUD Operations - Countries", description = "CRUD operations for Country entity")
public class CountryController {

    private final CountryService countryService;

    @PostMapping
    public ResponseEntity<ApiResponse> create(@Valid @RequestBody CountryRequest request, HttpServletRequest httpServletRequest) {
        CountryResponse response = countryService.create(request);
        ApiResponse apiResponse = ApiResponseUtil.created(response, "Country created successfully", httpServletRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> findById(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        CountryResponse response = countryService.findById(id);
        ApiResponse apiResponse = ApiResponseUtil.success(response, "Country retrieved successfully", httpServletRequest);
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> update(@PathVariable Long id, @Valid @RequestBody CountryRequest request, HttpServletRequest httpServletRequest) {
        CountryResponse response = countryService.update(id, request);
        ApiResponse apiResponse = ApiResponseUtil.success(response, "Country updated successfully", httpServletRequest);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        countryService.delete(id);
        ApiResponse apiResponse = ApiResponseUtil.success(null, "Country deleted successfully", httpServletRequest);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse> getAll(@RequestBody PageAndFilterDTO<CountryFilter> pageAndFilterDTO, HttpServletRequest httpServletRequest) {
        PaginationDTO<CountryResponse> response = countryService.getAll(pageAndFilterDTO);
        ApiResponse apiResponse = ApiResponseUtil.paginated(response, "Countries retrieved successfully", httpServletRequest);
        return ResponseEntity.ok(apiResponse);
    }
}
