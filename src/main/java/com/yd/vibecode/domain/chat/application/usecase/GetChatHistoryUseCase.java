package com.yd.vibecode.domain.chat.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.chat.application.dto.response.ChatHistoryResponse;
import com.yd.vibecode.domain.chat.domain.entity.PromptMessage;
import com.yd.vibecode.domain.chat.domain.entity.PromptSession;
import com.yd.vibecode.domain.chat.domain.service.PromptMessageService;
import com.yd.vibecode.domain.chat.domain.service.PromptSessionService;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetChatHistoryUseCase {

    private final PromptSessionService promptSessionService;
    private final PromptMessageService promptMessageService;

    @Transactional(readOnly = true)
    public ChatHistoryResponse execute(Long examId, Long participantId) {
        // 1. 세션 조회
        PromptSession session = promptSessionService.findByExamIdAndParticipantId(examId, participantId);
        
        if (session == null) {
            return null;
        }

        // 2. 메시지 목록 조회
        List<PromptMessage> messages = promptMessageService.findBySessionId(session.getId());

        // 3. Response 생성
        List<ChatHistoryResponse.MessageInfo> messageInfos = messages.stream()
                .map(msg -> new ChatHistoryResponse.MessageInfo(
                        msg.getId(),
                        msg.getTurn(),
                        msg.getRole(),
                        msg.getContent(),
                        msg.getTokenCount()
                ))
                .toList();

        return new ChatHistoryResponse(
                session.getId(),
                session.getExamId(),
                session.getParticipantId(),
                session.getTotalTokens(),
                messageInfos
        );
    }
}
