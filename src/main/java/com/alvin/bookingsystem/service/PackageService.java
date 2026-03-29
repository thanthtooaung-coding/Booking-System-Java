package com.alvin.bookingsystem.service;

import com.alvin.bookingsystem.dto.request.PurchasePackageRequest;
import com.alvin.bookingsystem.dto.response.AvailablePackageResponse;
import com.alvin.bookingsystem.dto.response.PurchasePackageResponse;
import com.alvin.bookingsystem.dto.response.UserPackageDetailResponse;

import java.util.List;

public interface PackageService {
    /**
     * Get available packages by country ID
     */
    List<AvailablePackageResponse> getAvailablePackagesByCountry(Long countryId);

    /**
     * Purchase a package
     */
    PurchasePackageResponse purchasePackage(Long userId, PurchasePackageRequest request);

    /**
     * Get user's purchased packages
     */
    List<UserPackageDetailResponse> getUserPackages(Long userId);

    /**
     * Get user package details by ID
     */
    UserPackageDetailResponse getUserPackageDetails(Long userId, Long userPackageId);
}
