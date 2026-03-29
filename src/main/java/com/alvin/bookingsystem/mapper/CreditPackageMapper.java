package com.alvin.bookingsystem.mapper;

import com.alvin.bookingsystem.domain.model.Country;
import com.alvin.bookingsystem.domain.model.CreditPackage;
import com.alvin.bookingsystem.dto.request.CreditPackageRequest;
import com.alvin.bookingsystem.dto.response.CreditPackageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreditPackageMapper {

    private final MasterDataMapper masterDataMapper;

    public CreditPackage toEntity(CreditPackageRequest request, Country country) {
        return CreditPackage.builder()
                .name(request.name())
                .country(country)
                .credits(request.credits())
                .price(request.price())
                .validityDays(request.validityDays())
                .description(request.description())
                .active(request.active() != null ? request.active() : true)
                .build();
    }

    public CreditPackageResponse toResponse(CreditPackage entity) {
        if (entity == null) {
            return null;
        }

        String countryName = null;
        if (entity.getCountry() != null) {
            countryName = entity.getCountry().getName();
        }

        return CreditPackageResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .countryId(entity.getCountry() != null ? entity.getCountry().getId() : null)
                .countryName(countryName)
                .credits(entity.getCredits())
                .price(entity.getPrice())
                .validityDays(entity.getValidityDays())
                .description(entity.getDescription())
                .active(entity.getActive())
                .masterData(masterDataMapper.toMasterData(entity))
                .build();
    }

    public void updateEntity(CreditPackage entity, CreditPackageRequest request, Country country) {
        if (request.name() != null) {
            entity.setName(request.name());
        }
        if (country != null) {
            entity.setCountry(country);
        }
        if (request.credits() != null) {
            entity.setCredits(request.credits());
        }
        if (request.price() != null) {
            entity.setPrice(request.price());
        }
        if (request.validityDays() != null) {
            entity.setValidityDays(request.validityDays());
        }
        if (request.description() != null) {
            entity.setDescription(request.description());
        }
        if (request.active() != null) {
            entity.setActive(request.active());
        }
    }
}
