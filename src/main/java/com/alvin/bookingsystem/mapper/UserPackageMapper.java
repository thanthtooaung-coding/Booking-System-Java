package com.alvin.bookingsystem.mapper;

import com.alvin.bookingsystem.domain.model.CreditPackage;
import com.alvin.bookingsystem.domain.model.User;
import com.alvin.bookingsystem.domain.model.UserPackage;
import com.alvin.bookingsystem.dto.request.UserPackageRequest;
import com.alvin.bookingsystem.dto.response.UserPackageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserPackageMapper {

    private final MasterDataMapper masterDataMapper;

    public UserPackage toEntity(UserPackageRequest request, User user, CreditPackage creditPackage) {
        return UserPackage.builder()
                .user(user)
                .creditPackage(creditPackage)
                .remainingCredits(creditPackage.getCredits())
                .purchasedAt(request.purchasedAt())
                .expiresAt(request.expiresAt())
                .status(UserPackage.Status.ACTIVE)
                .build();
    }

    public UserPackageResponse toResponse(UserPackage entity) {
        if (entity == null) {
            return null;
        }

        String userEmail = null;
        if (entity.getUser() != null) {
            userEmail = entity.getUser().getEmail();
        }

        String creditPackageName = null;
        if (entity.getCreditPackage() != null) {
            creditPackageName = entity.getCreditPackage().getName();
        }

        return UserPackageResponse.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userEmail(userEmail)
                .creditPackageId(entity.getCreditPackage() != null ? entity.getCreditPackage().getId() : null)
                .creditPackageName(creditPackageName)
                .remainingCredits(entity.getRemainingCredits())
                .purchasedAt(entity.getPurchasedAt())
                .expiresAt(entity.getExpiresAt())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .masterData(masterDataMapper.toMasterData(entity))
                .build();
    }

    public void updateEntity(UserPackage entity, UserPackageRequest request, User user, CreditPackage creditPackage) {
        if (user != null) {
            entity.setUser(user);
        }
        if (creditPackage != null) {
            entity.setCreditPackage(creditPackage);
        }
        if (request.purchasedAt() != null) {
            entity.setPurchasedAt(request.purchasedAt());
        }
        if (request.expiresAt() != null) {
            entity.setExpiresAt(request.expiresAt());
        }
    }
}
