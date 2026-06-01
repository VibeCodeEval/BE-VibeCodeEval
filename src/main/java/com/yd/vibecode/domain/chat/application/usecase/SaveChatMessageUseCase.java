package com.yd.vibecode.domain.chat.application.usecase;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.chat.application.dto.request.AISendMessageRequest;
import com.yd.vibecode.domain.chat.domain.entity.PromptMessage;
import com.yd.vibecode.domain.chat.application.dto.request.SaveChatMessageRequest;
import com.yd.vibecode.domain.chat.application.dto.response.SendMessageResponse;
import com.yd.vibecode.domain.chat.domain.entity.PromptSession;
import com.yd.vibecode.domain.chat.domain.service.PromptMessageService;
import com.yd.vibecode.domain.chat.domain.service.PromptSessionService;
import com.yd.vibecode.domain.chat.infrastructure.AIChatService;
import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.exam.domain.service.ExamParticipantService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.GlobalErrorStatus;
import com.yd.vibecode.global.exception.code.status.ProblemErrorStatus;

import lombok.RequiredArgsConstructor;

/**
 * 채팅 메시지 저장 UseCase
 *
 * 트랜잭션 경계:
 *  - 사전 저장(검증/세션/사용자 메시지): 각 서비스의 개별 트랜잭션에서 즉시 커밋
 *  - AI HTTP 호출: 트랜잭션 밖 (DB 커넥션 비점유)  ← 커넥션 풀 고갈 방지
 *  - 사후 저장(AI 메시지/토큰): ChatResponsePersister 단일 트랜잭션
 *
 * 주의: execute()가 REQUIRED 트랜잭션을 시작/전파받으면 AI 응답(최대 90초)을 기다리는 동안
 *       DB 커넥션을 점유하여 풀이 고갈될 수 있으므로, NOT_SUPPORTED로 외부 트랜잭션을 명시적으로 중단한다.
 */
@Service
@RequiredArgsConstructor
public class SaveChatMessageUseCase {

    private final PromptSessionService promptSessionService;
    private final PromptMessageService promptMessageService;
    private final ExamParticipantService examParticipantService;
    private final AIChatService aiChatService;
    private final ChatResponsePersister chatResponsePersister;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public SendMessageResponse execute(SaveChatMessageRequest request) {
        // 1. 참가자 검증 (읽기 트랜잭션 즉시 종료)
        ExamParticipant examParticipant = examParticipantService.findByExamIdAndParticipantId(
                request.examId(), request.participantId());
        if (examParticipant == null) {
            throw new RestApiException(GlobalErrorStatus._NOT_FOUND);
        }
        if (examParticipant.getSpecId() == null || examParticipant.getAssignedProblemId() == null) {
            throw new RestApiException(ProblemErrorStatus.NO_ASSIGNED_PROBLEM);
        }

        // 2. 세션 확보 (REQUIRES_NEW로 즉시 커밋 → AI 서버에서 조회 가능)
        PromptSession session;
        if (request.sessionId() != null) {
            session = promptSessionService.findByIdWithNewTransaction(request.sessionId());
            if (!session.getExamId().equals(request.examId())
                    || !session.getParticipantId().equals(request.participantId())) {
                throw new RestApiException(GlobalErrorStatus._NOT_FOUND);
            }
            promptSessionService.ensureSessionSpecId(session.getId(), examParticipant.getSpecId());
            session = promptSessionService.findByIdWithNewTransaction(request.sessionId());
        } else {
            session = promptSessionService.getOrCreateSessionWithNewTransaction(
                    request.examId(), request.participantId(), examParticipant.getSpecId());
        }

        // USER 메시지가 아니면 처리하지 않음 (FE는 USER만 전송)
        if (!"USER".equalsIgnoreCase(request.role())) {
            throw new RestApiException(GlobalErrorStatus._BAD_REQUEST);
        }

        // 3. 사용자 메시지 저장 — turn 계산 + INSERT를 단일 트랜잭션으로 처리 (레이스 컨디션 방지)
        PromptMessage savedUserMessage = promptMessageService.createWithNextTurn(
                session.getId(), request.role(), request.content(),
                request.tokenCount(), request.meta());
        Integer nextTurn = savedUserMessage.getTurn();
        if (request.tokenCount() != null && request.tokenCount() > 0) {
            promptSessionService.addTokens(session.getId(), request.tokenCount());
        }

        // 4. AI 호출 — 트랜잭션 밖 (DB 커넥션을 잡지 않음)
        Map<String, Object> context = new HashMap<>();
        context.put("problemId", examParticipant.getAssignedProblemId());
        context.put("specVersion", examParticipant.getSpecId());

        AISendMessageRequest aiRequest = AISendMessageRequest.builder()
                .sessionId(session.getId())
                .participantId(request.participantId())
                .turnId(nextTurn)
                .role("USER")
                .content(request.content())
                .context(context)
                .build();

        SendMessageResponse aiResponse;
        try {
            aiResponse = aiChatService.sendMessage(aiRequest);
        } catch (Exception e) {
            throw new RuntimeException(String.format("AI 서버 호출 실패: %s", e.getMessage()), e);
        }
        if (aiResponse == null) {
            throw new RuntimeException("AI 서버 응답이 null입니다.");
        }

        // 5. 사후 저장 — 단일 트랜잭션 (원자성 보장)
        chatResponsePersister.persistAiResponse(
                session.getId(), request.examId(), request.participantId(), aiResponse);

        return aiResponse;
    }
}
