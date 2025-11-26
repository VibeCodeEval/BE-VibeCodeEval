package com.yd.vibecode.domain.auth.domain.service;

import org.springframework.stereotype.Service;

import com.yd.vibecode.domain.auth.domain.entity.Participant;
import com.yd.vibecode.domain.auth.domain.repository.ParticipantRepository;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.GlobalErrorStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final ParticipantRepository participantRepository;

    public Participant findById(Long id) {
        return participantRepository.findById(id)
                .orElseThrow(() -> new RestApiException(GlobalErrorStatus._NOT_FOUND));
    }

    public Participant findByPhone(String phone) {
        return participantRepository.findByPhone(phone)
                .orElse(null);
    }

    public boolean existsByPhone(String phone) {
        return participantRepository.existsByPhone(phone);
    }

    public Participant create(String name, String phone) {
        Participant participant = Participant.builder()
                .name(name)
                .phone(phone)
                .build();
        return participantRepository.save(participant);
    }
}

