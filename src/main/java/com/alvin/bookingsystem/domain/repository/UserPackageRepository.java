package com.alvin.bookingsystem.domain.repository;

import com.alvin.bookingsystem.domain.model.UserPackage;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserPackageRepository extends BaseRepository<UserPackage> {
    
    @Query("SELECT up FROM UserPackage up WHERE up.status = 'ACTIVE' AND up.expiresAt < :currentTime")
    List<UserPackage> findExpiredActivePackages(@Param("currentTime") LocalDateTime currentTime);

    @Modifying
    @Query(value = "UPDATE user_packages SET created_by_id = :createdById WHERE id = :userPackageId", nativeQuery = true)
    void updateCreatedById(@Param("userPackageId") Long userPackageId, @Param("createdById") Long createdById);
}
