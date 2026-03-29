package com.alvin.bookingsystem.domain.repository;

import com.alvin.bookingsystem.domain.model.Country;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CountryRepository extends BaseRepository<Country> {
    Optional<Country> findByCode(String code);

    @Modifying
    @Query(value = "UPDATE countries SET created_by_id = :createdById WHERE id = :countryId", nativeQuery = true)
    void updateCreatedById(@Param("countryId") Long countryId, @Param("createdById") Long createdById);
}
