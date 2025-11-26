package com.yd.vibecode.domain.auth.domain.service;

import org.springframework.stereotype.Service;

import com.yd.vibecode.domain.auth.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.auth.domain.repository.ExamParticipantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExamParticipantService {

    private final ExamParticipantRepository examParticipantRepository;

    public ExamParticipant findByExamIdAndParticipantId(Long examId, Long participantId) {
        return examParticipantRepository.findByExamIdAndParticipantId(examId, participantId)
                .orElse(null);
    }

    public ExamParticipant findLatestByParticipantId(Long participantId) {
        return examParticipantRepository.findByParticipantIdOrderByJoinedAtDesc(participantId)
                .stream()
                .findFirst()
                .orElse(null);
    }

    public boolean existsByExamIdAndParticipantId(Long examId, Long participantId) {
        return examParticipantRepository.existsByExamIdAndParticipantId(examId, participantId);
    }

    public ExamParticipant create(Long examId, Long participantId, Long specId, Integer tokenLimit) {
        ExamParticipant examParticipant = ExamParticipant.builder()
                .examId(examId)
                .participantId(participantId)
                .specId(specId)
                .tokenLimit(tokenLimit != null ? tokenLimit : 20000)
                .tokenUsed(0)
                .build();

        return examParticipantRepository.save(examParticipant);
    }
}

