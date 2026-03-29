package com.alvin.bookingsystem.domain.repository;

import com.alvin.bookingsystem.domain.model.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends BaseRepository<User> {
    Optional<User> findByEmail(String email);
    
    @Modifying
    @Query(value = "UPDATE users SET created_by_id = :createdById WHERE id = :userId", nativeQuery = true)
    void updateCreatedById(@Param("userId") Long userId, @Param("createdById") Long createdById);
}
