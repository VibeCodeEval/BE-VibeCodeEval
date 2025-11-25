package com.yd.vibecode.domain.auth.domain.service;

import org.springframework.stereotype.Service;

import com.yd.vibecode.domain.auth.domain.entity.Participant;
import com.yd.vibecode.domain.auth.domain.repository.ParticipantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final ParticipantRepository participantRepository;

    public Participant findById(Long id) {
        return participantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("참가자를 찾을 수 없습니다."));
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

