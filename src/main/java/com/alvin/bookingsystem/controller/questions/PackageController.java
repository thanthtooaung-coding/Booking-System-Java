package com.alvin.bookingsystem.controller.questions;

import com.alvin.bookingsystem.dto.request.PurchasePackageRequest;
import com.alvin.bookingsystem.dto.response.ApiResponse;
import com.alvin.bookingsystem.dto.response.AvailablePackageResponse;
import com.alvin.bookingsystem.dto.response.PurchasePackageResponse;
import com.alvin.bookingsystem.dto.response.UserPackageDetailResponse;
import com.alvin.bookingsystem.service.PackageService;
import com.alvin.bookingsystem.util.ApiResponseUtil;
import com.alvin.bookingsystem.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/packages")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Question Operations - Package Module", description = "Package Module APIs (Get Available Packages, Purchase Package, Get User Packages)")
public class PackageController {

    private final PackageService packageService;

    /**
     * Get Available Packages by Country
     * GET /api/packages/available?countryId={countryId}
     */
    @GetMapping("/available")
    @Operation(
            summary = "Get available packages by country",
            description = "Retrieves a list of all available credit packages for a specific country. Returns package details including name, description, price, credits, and validity days. No authentication required."
    )
    public ResponseEntity<ApiResponse> getAvailablePackagesByCountry(
            @RequestParam Long countryId,
            HttpServletRequest httpServletRequest) {
        List<AvailablePackageResponse> response = packageService.getAvailablePackagesByCountry(countryId);
        ApiResponse apiResponse = ApiResponseUtil.success(
                response,
                "Available packages retrieved successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Purchase Package
     * POST /api/packages/purchase
     */
    @PostMapping("/purchase")
    @Operation(
            summary = "Purchase a package",
            description = "Purchases a credit package for the authenticated user. The package will be associated with the user's account and credits will be available immediately. Requires authentication."
    )
    public ResponseEntity<ApiResponse> purchasePackage(
            @Valid @RequestBody PurchasePackageRequest request,
            HttpServletRequest httpServletRequest) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("User not authenticated");
        }

        PurchasePackageResponse response = packageService.purchasePackage(userId, request);
        ApiResponse apiResponse = ApiResponseUtil.created(
                response,
                "Package purchased successfully",
                httpServletRequest
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    /**
     * Get User's Purchased Packages
     * GET /api/packages/my-packages
     */
    @GetMapping("/my-packages")
    @Operation(
            summary = "Get user's purchased packages",
            description = "Retrieves a list of all packages purchased by the authenticated user. Returns package details including status, remaining credits, expiration date, and purchase date. Requires authentication."
    )
    public ResponseEntity<ApiResponse> getUserPackages(HttpServletRequest httpServletRequest) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("User not authenticated");
        }

        List<UserPackageDetailResponse> response = packageService.getUserPackages(userId);
        ApiResponse apiResponse = ApiResponseUtil.success(
                response,
                "User packages retrieved successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get User Package Details
     * GET /api/packages/{userPackageId}
     */
    @GetMapping("/{userPackageId}")
    @Operation(
            summary = "Get user package details",
            description = "Retrieves detailed information about a specific user package by its ID. Includes package information, remaining credits, status, expiration date, and purchase history. Requires authentication and the package must belong to the authenticated user."
    )
    public ResponseEntity<ApiResponse> getUserPackageDetails(
            @PathVariable Long userPackageId,
            HttpServletRequest httpServletRequest) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("User not authenticated");
        }

        UserPackageDetailResponse response = packageService.getUserPackageDetails(userId, userPackageId);
        ApiResponse apiResponse = ApiResponseUtil.success(
                response,
                "User package details retrieved successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }
}
