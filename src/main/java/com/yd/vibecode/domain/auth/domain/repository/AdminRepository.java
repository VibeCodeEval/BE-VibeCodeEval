package com.yd.vibecode.domain.auth.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yd.vibecode.domain.auth.domain.entity.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByAdminNumber(String adminNumber);

    Optional<Admin> findByEmail(String email);

    boolean existsByAdminNumber(String adminNumber);

    boolean existsByEmail(String email);
}

