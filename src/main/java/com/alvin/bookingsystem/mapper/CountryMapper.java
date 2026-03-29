package com.alvin.bookingsystem.mapper;

import com.alvin.bookingsystem.domain.model.Country;
import com.alvin.bookingsystem.dto.request.CountryRequest;
import com.alvin.bookingsystem.dto.response.CountryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CountryMapper {

    private final MasterDataMapper masterDataMapper;

    public Country toEntity(CountryRequest request) {
        return Country.builder()
                .code(request.code())
                .name(request.name())
                .active(request.active() != null ? request.active() : true)
                .build();
    }

    public CountryResponse toResponse(Country entity) {
        if (entity == null) {
            return null;
        }
        
        return CountryResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .active(entity.getActive())
                .masterData(masterDataMapper.toMasterData(entity))
                .build();
    }

    public void updateEntity(Country entity, CountryRequest request) {
        if (request.code() != null) {
            entity.setCode(request.code());
        }
        if (request.name() != null) {
            entity.setName(request.name());
        }
        if (request.active() != null) {
            entity.setActive(request.active());
        }
    }
}
