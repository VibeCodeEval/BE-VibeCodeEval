package com.yd.vibecode.domain.auth.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yd.vibecode.domain.auth.domain.entity.EntryCode;

public interface EntryCodeRepository extends JpaRepository<EntryCode, String> {

    Optional<EntryCode> findByCode(String code);

    List<EntryCode> findByExamId(Long examId);

    List<EntryCode> findByExamIdAndIsActive(Long examId, Boolean isActive);
}

