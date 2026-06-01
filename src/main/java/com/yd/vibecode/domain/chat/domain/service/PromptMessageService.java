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

    /**
     * turn 계산과 메시지 저장을 단일 트랜잭션으로 처리.
     * getNextTurn + create를 별도 트랜잭션으로 분리하면 동시 요청이 같은 turn 값을
     * 읽어 (session_id, turn) 유니크 제약 위반이 발생할 수 있으므로 반드시 이 메서드를 사용한다.
     */
    @Transactional
    public PromptMessage createWithNextTurn(Long sessionId, String role, String content,
                                            Integer tokenCount, String meta) {
        Integer nextTurn = getNextTurn(sessionId);
        return create(sessionId, nextTurn, role, content, tokenCount, meta);
    }
}
