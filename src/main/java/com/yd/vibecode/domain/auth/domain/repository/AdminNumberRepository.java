package com.yd.vibecode.domain.auth.domain.repository;

import com.yd.vibecode.domain.auth.domain.entity.AdminNumber;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminNumberRepository extends JpaRepository<AdminNumber, Long> {

    Optional<AdminNumber> findByAdminNumber(String adminNumber);

    List<AdminNumber> findByAdminNumberIn(Collection<String> adminNumbers);

    boolean existsByAdminNumber(String adminNumber);
}


