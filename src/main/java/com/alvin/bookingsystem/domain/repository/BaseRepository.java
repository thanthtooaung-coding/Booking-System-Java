package com.alvin.bookingsystem.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseRepository<ENTITY> extends JpaRepository<ENTITY, Long>, JpaSpecificationExecutor<ENTITY> {
}
