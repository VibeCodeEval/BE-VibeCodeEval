package com.yd.vibecode.domain.chat.ui;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.yd.vibecode.domain.chat.application.dto.request.SaveChatMessageRequest;
import com.yd.vibecode.domain.chat.application.dto.response.SendMessageResponse;
import com.yd.vibecode.domain.chat.application.usecase.SaveChatMessageUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class ChatMessageAsyncDispatcherTest {

    @InjectMocks private ChatMessageAsyncDispatcher dispatcher;
    @Mock private SaveChatMessageUseCase saveChatMessageUseCase;
    @Mock private SimpMessagingTemplate messagingTemplate;

    private SaveChatMessageRequest request() {
        return new SaveChatMessageRequest(null, 1L, 10L, 1, "user", "질문", null, null);
    }

    @Test
    @DisplayName("성공 시 /queue/chat 으로 응답을 push 한다")
    void dispatchSuccess() {
        SendMessageResponse response = new SendMessageResponse(1L, 2, "AI", "answer", 50, 150);
        given(saveChatMessageUseCase.execute(any())).willReturn(response);

        dispatcher.dispatch(request());

        verify(messagingTemplate).convertAndSendToUser(eq("10"), eq("/queue/chat"), eq(response));
    }

    @Test
    @DisplayName("실패 시 /queue/chat-error 로 에러를 push 한다")
    void dispatchFailure() {
        given(saveChatMessageUseCase.execute(any()))
                .willThrow(new RuntimeException("Could not open JPA EntityManager for transaction"));

        dispatcher.dispatch(request());

        verify(messagingTemplate).convertAndSendToUser(eq("10"), eq("/queue/chat-error"), any(Object.class));
    }
}
