package com.alvin.bookingsystem.mapper;

import com.alvin.bookingsystem.domain.model.MasterEntity;
import com.alvin.bookingsystem.dto.response.MasterData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MasterDataMapper {

    public MasterData toMasterData(MasterEntity entity) {
        return MasterData.builder()
                .id(entity.getId())
                .createdBy(entity.getCreatedById())
                .updatedBy(entity.getUpdatedById())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
