package com.alvin.bookingsystem.service.impl;

import com.alvin.bookingsystem.domain.dao.PackageDao;
import com.alvin.bookingsystem.domain.model.CreditPackage;
import com.alvin.bookingsystem.domain.model.User;
import com.alvin.bookingsystem.domain.model.UserPackage;
import com.alvin.bookingsystem.domain.repository.CreditPackageRepository;
import com.alvin.bookingsystem.domain.repository.UserPackageRepository;
import com.alvin.bookingsystem.domain.repository.UserRepository;
import com.alvin.bookingsystem.dto.request.PurchasePackageRequest;
import com.alvin.bookingsystem.dto.response.AvailablePackageResponse;
import com.alvin.bookingsystem.dto.response.PurchasePackageResponse;
import com.alvin.bookingsystem.dto.response.UserPackageDetailResponse;
import com.alvin.bookingsystem.exception.CustomException;
import com.alvin.bookingsystem.service.PackageService;
import com.alvin.bookingsystem.util.MockService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PackageServiceImpl implements PackageService {

    private final PackageDao packageDao;
    private final CreditPackageRepository creditPackageRepository;
    private final UserPackageRepository userPackageRepository;
    private final UserRepository userRepository;
    private final MockService mockService;

    @Override
    public List<AvailablePackageResponse> getAvailablePackagesByCountry(Long countryId) {
        return packageDao.findAvailablePackagesByCountryId(countryId);
    }

    @Override
    @Transactional
    public PurchasePackageResponse purchasePackage(Long userId, PurchasePackageRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        CreditPackage creditPackage = creditPackageRepository.findById(request.creditPackageId())
                .orElseThrow(() -> new EntityNotFoundException("Credit package not found with id: " + request.creditPackageId()));

        if (!creditPackage.getActive()) {
            throw new CustomException("Credit package is not active");
        }

        if (creditPackage.getCountry() == null || !creditPackage.getCountry().getActive()) {
            throw new CustomException("Country for this package is not active");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusDays(creditPackage.getValidityDays());

        UserPackage userPackage = UserPackage.builder()
                .user(user)
                .creditPackage(creditPackage)
                .remainingCredits(creditPackage.getCredits())
                .purchasedAt(now)
                .expiresAt(expiresAt)
                .status(UserPackage.Status.ACTIVE)
                .build();

        userPackage.setCreatedById(userId);
        userPackage = userPackageRepository.save(userPackage);

        try {
            boolean paymentSuccess = mockService.paymentCharge(
                    creditPackage.getPrice().longValue(),
                    "Purchase package: " + creditPackage.getName(),
                    null
            );
            if (!paymentSuccess) {
                throw new CustomException("Payment failed. Please try again.");
            }
        } catch (Exception e) {
            log.error("Payment charge failed for user {} and package {}", userId, request.creditPackageId(), e);
            throw new CustomException("Payment failed: " + e.getMessage());
        }

        return PurchasePackageResponse.builder()
                .id(userPackage.getId())
                .userId(userPackage.getUser().getId())
                .creditPackageId(userPackage.getCreditPackage().getId())
                .creditPackageName(userPackage.getCreditPackage().getName())
                .credits(userPackage.getCreditPackage().getCredits())
                .remainingCredits(userPackage.getRemainingCredits())
                .purchasedAt(userPackage.getPurchasedAt())
                .expiresAt(userPackage.getExpiresAt())
                .status(userPackage.getStatus().name())
                .build();
    }

    @Override
    public List<UserPackageDetailResponse> getUserPackages(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        return packageDao.findUserPackages(userId);
    }

    @Override
    public UserPackageDetailResponse getUserPackageDetails(Long userId, Long userPackageId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        UserPackageDetailResponse userPackage = packageDao.findUserPackageById(userPackageId);
        
        if (userPackage == null) {
            throw new EntityNotFoundException("User package not found with id: " + userPackageId);
        }

        if (!userPackage.userId().equals(userId)) {
            throw new CustomException("User package does not belong to the current user");
        }

        return userPackage;
    }
}
