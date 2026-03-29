package com.alvin.bookingsystem.domain.repository;

import com.alvin.bookingsystem.domain.model.CreditPackage;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CreditPackageRepository extends BaseRepository<CreditPackage> {
    Optional<CreditPackage> findByNameAndCountryId(String name, Long countryId);

    @Modifying
    @Query(value = "UPDATE credit_packages SET created_by_id = :createdById WHERE id = :creditPackageId", nativeQuery = true)
    void updateCreatedById(@Param("creditPackageId") Long creditPackageId, @Param("createdById") Long createdById);
}
