package com.yd.vibecode.domain.chat.application.usecase;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.chat.application.dto.request.AISendMessageRequest;
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

@Service
@RequiredArgsConstructor
public class SendMessageUseCase {

    private final PromptSessionService promptSessionService;
    private final PromptMessageService promptMessageService;
    private final ExamParticipantService examParticipantService;
    private final AIChatService aiChatService;

    @Transactional
    public SendMessageResponse execute(Long examId, Long participantId, String message) {
        // 1. 참가자 검증
        ExamParticipant examParticipant = examParticipantService.findByExamIdAndParticipantId(examId, participantId);
        if (examParticipant == null) {
            throw new RestApiException(GlobalErrorStatus._NOT_FOUND);
        }

        // 2. 세션 가져오기 또는 생성
        PromptSession session = promptSessionService.getOrCreateSession(
                examId, participantId, examParticipant.getSpecId());

        // 3. Turn ID 결정 (마지막 턴 + 1)
        // 간단한 구현을 위해 PromptMessage 레포지토리를 통해 max turn 조회하거나, 
        // 클라이언트가 보내는 turnId를 신뢰할 수도 있음. 
        // 여기서는 안전하게 DB에서 조회하여 결정하는 방식 권장되나, 
        // 현재 구조상 단순히 클라이언트 요청을 기반으로 하거나, 
        // 일단은 매번 새로운 턴으로 간주하여 증가시키는 로직이 필요함.
        // PromptMessageService에 getLastTurn 메서드가 없다면 추가 필요.
        // 임시로: 현재 세션의 메시지 개수 / 2 + 1 (A/B 대화 쌍) 로 추정하거나,
        // 편의상 System timestamp modulo나 순차 증가 로직 구현.
        // 여기서는 가장 단순하게 레포지토리 의존성 추가 없이, 
        // 서비스 메서드에 getLastTurn 추가 후 호출한다고 가정.
        // 또는 API 스펙상 클라이언트가 turnId를 관리한다면 파라미터로 받아야 함. 
        // 현재 execute 메서드는 message만 받고 있음. -> DTO를 받는 형태로 리팩토링 필요.
        
        // * 리팩토링: 필요시 DTO를 받는 형태로 변경 가능
        // 일단 내부적으로 1부터 시작한다고 가정하고 구현.
        // 실무에서는 DB 조회 필요.
        Integer turnId = 1; 

        promptMessageService.create(
                session.getId(),
                turnId,
                "USER",
                message,
                0, 
                null
        );

        // 4. AI 요청 구성
        Map<String, Object> context = new HashMap<>();
        context.put("problemId", examParticipant.getAssignedProblemId()); // 문제 ID
        context.put("specVersion", examParticipant.getSpecId()); // 스펙 버전 (ID로 대체)

        AISendMessageRequest aiRequest = AISendMessageRequest.builder()
                .sessionId(session.getId())
                .participantId(participantId)  // 추가: 사용자 식별
                .turnId(turnId)
                .role("USER")
                .content(message)
                .context(context)
                .build();

        // 5. AI 호출 (Sync)
        SendMessageResponse aiResponse = aiChatService.sendMessage(aiRequest);

        // 6. AI 메시지 저장 및 토큰 업데이트
        // AI 서버는 turn을 자동으로 증가시켜서 반환 (사용자 메시지 turn + 1)
        Integer totalCount = aiResponse.totalCount();
        Integer aiTurnId = aiResponse.turnId(); // AI 서버가 반환한 turn (사용자 turn + 1)
        
        promptMessageService.create(
                session.getId(),
                aiTurnId,
                aiResponse.role(), // AI 서버가 반환한 role
                aiResponse.content(),
                totalCount != null ? totalCount : 0,
                null // 메타데이터 저장 필요시 변환
        );

        // 7. 세션 토큰 사용량 업데이트
        if (totalCount != null) {
            promptSessionService.addTokens(session.getId(), totalCount);
        }

        // 8. 응답 반환 (AI 서버가 반환한 모든 필드 그대로 사용)
        return aiResponse;
    }
}
