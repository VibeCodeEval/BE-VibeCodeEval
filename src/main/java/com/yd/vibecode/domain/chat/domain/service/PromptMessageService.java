package com.yd.vibecode.domain.chat.domain.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.chat.domain.entity.PromptMessage;
import com.yd.vibecode.domain.chat.domain.repository.PromptMessageRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PromptMessageService {

    private final PromptMessageRepository promptMessageRepository;

    @Transactional
    public PromptMessage create(Long sessionId, Integer turn, String role, String content,
                                 Integer tokenCount, String meta) {
        PromptMessage message = PromptMessage.builder()
                .sessionId(sessionId)
                .turn(turn)
                .role(role)
                .content(content)
                .tokenCount(tokenCount)
                .meta(meta)
                .build();
        return promptMessageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public List<PromptMessage> findBySessionId(Long sessionId) {
        return promptMessageRepository.findBySessionIdOrderByTurnAsc(sessionId);
    }
}
