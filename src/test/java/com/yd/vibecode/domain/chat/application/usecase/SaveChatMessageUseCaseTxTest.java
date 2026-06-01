package com.yd.vibecode.domain.chat.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.yd.vibecode.domain.chat.application.dto.request.SaveChatMessageRequest;
import com.yd.vibecode.domain.chat.application.dto.response.SendMessageResponse;
import com.yd.vibecode.domain.chat.domain.entity.PromptMessage;
import com.yd.vibecode.domain.chat.domain.repository.PromptMessageRepository;
import com.yd.vibecode.domain.chat.domain.service.PromptMessageService;
import com.yd.vibecode.domain.chat.domain.service.PromptSessionService;
import com.yd.vibecode.domain.chat.infrastructure.AIChatService;
import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.exam.domain.repository.ExamParticipantRepository;
import com.yd.vibecode.domain.exam.domain.service.ExamParticipantService;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * SaveChatMessageUseCase 의 트랜잭션 경계 검증.
 *
 * 핵심: 동기 AI HTTP 호출이 DB 트랜잭션(=커넥션) 밖에서 일어나야 한다.
 * 그렇지 않으면 AI 응답(최대 90초)을 기다리는 동안 커넥션을 점유해 풀이 고갈된다.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
// application-secret.yml 이 상속시키는 default_schema(ai_vibe_coding_test)를 비워 H2 기본 스키마 사용
@TestPropertySource(properties = "spring.jpa.properties.hibernate.default_schema=")
@Import({SaveChatMessageUseCase.class, ChatResponsePersister.class,
        PromptSessionService.class, PromptMessageService.class, ExamParticipantService.class})
class SaveChatMessageUseCaseTxTest {

    @Autowired private SaveChatMessageUseCase saveChatMessageUseCase;
    @Autowired private ExamParticipantRepository examParticipantRepository;
    @Autowired private PromptMessageRepository promptMessageRepository;

    @MockBean private AIChatService aiChatService;

    @Test
    @DisplayName("AI 호출 시점에 활성 트랜잭션이 없어야 한다(커넥션 비점유)")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void aiCallRunsOutsideTransaction() {
        // given: 참가자 시드 (specId, assignedProblemId 필수)
        examParticipantRepository.save(
                ExamParticipant.builder()
                        .examId(1L).participantId(10L)
                        .specId(100L).tokenLimit(20000).tokenUsed(0)
                        .assignedProblemId(1000L)
                        .build());

        AtomicBoolean txActiveDuringAiCall = new AtomicBoolean(true);
        given(aiChatService.sendMessage(any())).willAnswer(inv -> {
            txActiveDuringAiCall.set(TransactionSynchronizationManager.isActualTransactionActive());
            return new SendMessageResponse(1L, 2, "AI", "answer", 50, 150);
        });

        SaveChatMessageRequest request = new SaveChatMessageRequest(
                null, 1L, 10L, 1, "user", "질문입니다", null, null);

        // when
        SendMessageResponse response = saveChatMessageUseCase.execute(request);

        // then: AI 호출 중 활성 트랜잭션이 없어야 한다
        assertThat(txActiveDuringAiCall.get()).isFalse();
        assertThat(response.content()).isEqualTo("answer");

        // 사용자 메시지 + AI 메시지 모두 영속화
        List<PromptMessage> messages = promptMessageRepository.findAll();
        assertThat(messages).extracting(PromptMessage::getRole)
                .containsExactlyInAnyOrder("user", "AI");
    }
}
