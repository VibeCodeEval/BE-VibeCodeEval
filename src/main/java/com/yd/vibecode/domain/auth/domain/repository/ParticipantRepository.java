package com.yd.vibecode.domain.auth.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yd.vibecode.domain.auth.domain.entity.Participant;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    Optional<Participant> findByPhone(String phone);

    boolean existsByPhone(String phone);
}

