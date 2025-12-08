package com.yd.vibecode.domain.chat.ui;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yd.vibecode.domain.chat.application.dto.request.SaveChatMessageRequest;
import com.yd.vibecode.domain.chat.application.dto.request.UpdateTokenUsageRequest;
import com.yd.vibecode.domain.chat.application.dto.response.ChatHistoryResponse;
import com.yd.vibecode.domain.chat.application.usecase.GetChatHistoryUseCase;
import com.yd.vibecode.domain.chat.application.usecase.SaveChatMessageUseCase;
import com.yd.vibecode.domain.chat.application.usecase.UpdateTokenUsageUseCase;
import com.yd.vibecode.global.common.BaseResponse;
import com.yd.vibecode.global.swagger.ChatApi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Chat Controller
 * - 대화 저장 및 조회
 * - 토큰 사용량 업데이트 (AI 콜백)
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController implements ChatApi {

    private final SaveChatMessageUseCase saveChatMessageUseCase;
    private final GetChatHistoryUseCase getChatHistoryUseCase;
    private final UpdateTokenUsageUseCase updateTokenUsageUseCase;

    @PostMapping("/messages")
    public BaseResponse<Void> saveChatMessage(@Valid @RequestBody SaveChatMessageRequest request) {
        saveChatMessageUseCase.execute(request);
        return BaseResponse.onSuccess();
    }

    @GetMapping("/history")
    public BaseResponse<ChatHistoryResponse> getChatHistory(
            @RequestParam Long examId,
            @RequestParam Long participantId) {
        ChatHistoryResponse response = getChatHistoryUseCase.execute(examId, participantId);
        return BaseResponse.onSuccess(response);
    }

    @PostMapping("/tokens/update")
    public BaseResponse<Void> updateTokenUsage(@Valid @RequestBody UpdateTokenUsageRequest request) {
        updateTokenUsageUseCase.execute(request);
        return BaseResponse.onSuccess();
    }
}
