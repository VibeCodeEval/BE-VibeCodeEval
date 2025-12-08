package com.yd.vibecode.domain.chat.domain.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.chat.domain.entity.PromptMessage;
import com.yd.vibecode.domain.chat.domain.repository.PromptMessageRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

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

    /**
     * 세션의 다음 turn 값을 계산
     * @param sessionId 세션 ID
     * @return 다음 turn 값 (메시지가 없으면 1)
     */
    @Transactional(readOnly = true)
    public Integer getNextTurn(Long sessionId) {
        Optional<Integer> maxTurn = promptMessageRepository.findMaxTurnBySessionId(sessionId);
        return maxTurn.map(turn -> turn + 1).orElse(1);
    }
}
