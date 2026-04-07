package com.yd.vibecode.domain.chat.application.usecase;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.chat.application.dto.request.AISendMessageRequest;
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

import lombok.RequiredArgsConstructor;

/**
 * 채팅 메시지 저장 UseCase
 * - 사용자 메시지 저장
 * - 사용자 메시지인 경우 AI 서버로 전송 및 AI 응답 저장
 */
@Service
@RequiredArgsConstructor
public class SaveChatMessageUseCase {

    private final PromptSessionService promptSessionService;
    private final PromptMessageService promptMessageService;
    private final ExamParticipantService examParticipantService;
    private final AIChatService aiChatService;

    @Transactional
    public SendMessageResponse execute(SaveChatMessageRequest request) {
        // 1. ExamParticipant 조회하여 specId 가져오기
        ExamParticipant examParticipant = examParticipantService.findByExamIdAndParticipantId(
                request.examId(), request.participantId());

        if (examParticipant == null) {
            throw new RestApiException(GlobalErrorStatus._NOT_FOUND);
        }

        // 2. 세션 가져오기 또는 생성 (별도 트랜잭션으로 즉시 커밋)
        // AI 서버에서 세션을 조회할 수 있도록 별도 트랜잭션으로 처리하여 즉시 커밋
        PromptSession session;
        if (request.sessionId() != null) {
            // sessionId가 제공되면 해당 세션 사용 (별도 트랜잭션으로 조회)
            session = promptSessionService.findByIdWithNewTransaction(request.sessionId());
            // 세션이 요청한 examId와 participantId와 일치하는지 검증
            if (!session.getExamId().equals(request.examId()) || 
                !session.getParticipantId().equals(request.participantId())) {
                throw new RestApiException(GlobalErrorStatus._NOT_FOUND);
            }
        } else {
            // sessionId가 없으면 examId와 participantId로 세션 조회/생성 (별도 트랜잭션으로 즉시 커밋)
            session = promptSessionService.getOrCreateSessionWithNewTransaction(
                    request.examId(), request.participantId(), examParticipant.getSpecId());
        }

        // 3. 다음 turn 계산 (사용자가 보낸 turn은 무시하고 DB에서 자동 계산)
        Integer nextTurn = promptMessageService.getNextTurn(session.getId());

        // 4. 사용자 메시지 저장
        promptMessageService.create(
                session.getId(),
                nextTurn,
                request.role(),
                request.content(),
                request.tokenCount(),
                request.meta()
        );

        // 4. 토큰 카운트가 있으면 세션에 누적
        if (request.tokenCount() != null && request.tokenCount() > 0) {
            promptSessionService.addTokens(session.getId(), request.tokenCount());
        }

        // 5. 사용자 메시지인 경우 AI 서버로 전송
        if ("USER".equalsIgnoreCase(request.role()) || "user".equalsIgnoreCase(request.role())) {
            try {
                // AI 요청 구성
                Map<String, Object> context = new HashMap<>();
                context.put("problemId", examParticipant.getAssignedProblemId()); // 문제 ID
                context.put("specVersion", examParticipant.getSpecId()); // 스펙 ID

                AISendMessageRequest aiRequest = AISendMessageRequest.builder()
                        .sessionId(session.getId())
                        .participantId(request.participantId())
                        .turnId(nextTurn)  // 계산된 turn 사용
                        .role("USER")
                        .content(request.content())
                        .context(context)
                        .build();

                // AI 호출 (Sync)
                SendMessageResponse aiResponse = aiChatService.sendMessage(aiRequest);

                if (aiResponse == null) {
                    throw new RuntimeException("AI 서버 응답이 null입니다.");
                }

                // AI 메시지 저장 및 토큰 업데이트
                // AI 서버는 turn을 자동으로 증가시켜서 반환 (사용자 메시지 turn + 1)
                Integer totalCount = aiResponse.totalCount();
                Integer aiTurnId = aiResponse.turnId(); // AI 서버가 반환한 turn (사용자 turn + 1)
                Integer tokenCount = aiResponse.tokenCount(); // 현재 턴 토큰 수

                // AI 응답 메시지 저장
                promptMessageService.create(
                        session.getId(),
                        aiTurnId,
                        aiResponse.role() != null ? aiResponse.role() : "AI", // AI 서버가 반환한 role
                        aiResponse.content() != null ? aiResponse.content() : "",
                        tokenCount != null ? tokenCount : 0, // 현재 턴 토큰 수 저장
                        null // 메타데이터 저장 필요시 변환
                );

                // 세션 토큰 사용량 업데이트
                // tokenCount는 현재 턴에서 사용한 토큰 수이므로 이를 세션에 누적
                if (tokenCount != null && tokenCount > 0) {
                    promptSessionService.addTokens(session.getId(), tokenCount);
                }

                // ExamParticipant의 tokenUsed도 업데이트 (토큰 한도 체크용)
                // tokenCount는 현재 턴에서 사용한 토큰 수이므로 이를 추가
                // 주의: totalCount는 전체 누적 값이므로 사용하지 않음
                if (tokenCount != null && tokenCount > 0) {
                    examParticipantService.addTokenUsed(
                            request.examId(), 
                            request.participantId(), 
                            tokenCount);
                }
                
                // 참고: FastAPI에서 반환하는 totalToken은 전체 누적 토큰 수이며,
                // FE에서 토큰 표시용으로 사용됩니다. 서버에서는 tokenCount만 누적합니다.

                // AI 응답 반환
                return aiResponse;
                
            } catch (Exception e) {
                // AI 호출 실패 시 로그 남기고 예외 전파
                // 예외는 상위로 전파되어 500 에러로 반환됨
                throw new RuntimeException(
                        String.format("AI 서버 호출 실패: %s", e.getMessage()), e);
            }
        }

        // 사용자 메시지가 아닌 경우 (예: assistant 메시지를 직접 저장하는 경우)
        // FE에서는 USER 메시지만 보내므로 이 경우는 발생하지 않지만, 안전을 위해 처리
        // null 반환 시 BaseResponse.onSuccess(null)이 호출되어 result가 null이 됨
        // FE에서는 이를 처리할 수 있지만, 명시적으로 에러를 반환하는 것이 좋음
        throw new RestApiException(GlobalErrorStatus._BAD_REQUEST);
    }
}
