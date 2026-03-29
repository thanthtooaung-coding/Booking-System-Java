package com.alvin.bookingsystem.domain.repository;

import com.alvin.bookingsystem.domain.model.ClassDefinition;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassDefinitionRepository extends BaseRepository<ClassDefinition> {
    @Modifying
    @Query(value = "UPDATE class_definitions SET created_by_id = :createdById WHERE id = :classDefinitionId", nativeQuery = true)
    void updateCreatedById(@Param("classDefinitionId") Long classDefinitionId, @Param("createdById") Long createdById);
}
