package com.alvin.bookingsystem.mapper;

import com.alvin.bookingsystem.domain.model.User;
import com.alvin.bookingsystem.dto.request.UserRequest;
import com.alvin.bookingsystem.dto.response.UserResponse;
import com.alvin.bookingsystem.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final MasterDataMapper masterDataMapper;

    public User toEntity(UserRequest request) {
        return User.builder()
                .email(request.email())
                .password(PasswordUtil.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .emailVerified(false)
                .active(true)
                .build();
    }

    public UserResponse toResponse(User entity) {
        if (entity == null) {
            return null;
        }
        
        return UserResponse.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .phone(entity.getPhone())
                .emailVerified(entity.getEmailVerified())
                .active(entity.getActive())
                .masterData(masterDataMapper.toMasterData(entity))
                .build();
    }

    public void updateEntity(User entity, UserRequest request) {
        if (request.email() != null) {
            entity.setEmail(request.email());
        }
        if (request.password() != null && !request.password().isEmpty()) {
            entity.setPassword(PasswordUtil.encode(request.password()));
        }
        if (request.firstName() != null) {
            entity.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            entity.setLastName(request.lastName());
        }
        if (request.phone() != null) {
            entity.setPhone(request.phone());
        }
    }
}
