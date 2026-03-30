package com.yd.vibecode.domain.submission.application.usecase;

import com.yd.vibecode.domain.submission.domain.entity.Submission;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.yd.vibecode.domain.chat.domain.service.PromptSessionService;
import com.yd.vibecode.domain.chat.infrastructure.AIChatService;
import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.exam.domain.service.ExamParticipantService;
import com.yd.vibecode.domain.submission.application.dto.request.AISubmitEvaluationRequest;
import com.yd.vibecode.domain.submission.application.dto.request.SubmitRequest;
import com.yd.vibecode.domain.submission.application.dto.response.SubmitResponse;
import com.yd.vibecode.domain.submission.domain.service.OutboxEventService;
import com.yd.vibecode.domain.submission.domain.service.SubmissionService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.ProblemErrorStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 제출 UseCase
 * - 코드 검증 및 해시 계산
 * - DB 저장
 * - Redis Queue에 enqueue (채점 워커가 dequeue하여 처리)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubmitUseCase {

    private final ExamParticipantService examParticipantService;
    private final SubmissionService submissionService;
    private final PromptSessionService promptSessionService;
    private final OutboxEventService outboxEventService;

    @Transactional
    public SubmitResponse execute(Long examId, Long userId, SubmitRequest request) {
        // 1. ExamParticipant 조회 및 specId 확인
        ExamParticipant examParticipant = examParticipantService.findByExamIdAndParticipantId(examId, userId);
        
        if (examParticipant == null || examParticipant.getSpecId() == null) {
            throw new RestApiException(ProblemErrorStatus.NO_ASSIGNED_PROBLEM);
        }

        // 2. 제출 생성 및 Redis Queue enqueue
        Submission submission = submissionService.createAndEnqueue(
            examId,
            userId,
            examParticipant.getSpecId(),
            request.lang(),
            request.code()
        );

        // 3. 세션 생성 (트랜잭션 내에서 먼저 생성하여 커밋 보장)
        promptSessionService.getOrCreateSession(
                examId, userId, examParticipant.getSpecId());

        // 4. AI 평가 요청을 Outbox에 저장 (같은 트랜잭션 내에서 처리되어 유실 방지)
        AISubmitEvaluationRequest aiRequest = new AISubmitEvaluationRequest(
                examId,
                userId,  // participantId
                examParticipant.getAssignedProblemId(),
                examParticipant.getSpecId(),
                request.code(),
                request.lang(),
                submission.getId()
        );

        outboxEventService.saveEvent(
                "SUBMISSION",
                submission.getId(),
                "AI_EVAL_REQUEST",
                aiRequest
        );

        log.info("AI evaluation request saved to outbox: submissionId={}", submission.getId());

        // 5. 응답 반환 (202 Accepted)
        return new SubmitResponse(submission.getId(), submission.getStatus());
    }
}
