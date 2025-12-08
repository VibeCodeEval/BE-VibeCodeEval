package com.yd.vibecode.domain.submission.application.usecase;

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
import com.yd.vibecode.domain.submission.domain.entity.Submission;
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
    private final AIChatService aiChatService;

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

        // 4. 트랜잭션 커밋 후 AI 서버로 평가 요청 전송
        // 세션이 DB에 커밋된 후 AI 서버에서 조회할 수 있도록 트랜잭션 커밋 후 실행
        TransactionSynchronizationManager.registerSynchronization(
            new org.springframework.transaction.support.TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        AISubmitEvaluationRequest aiRequest = new AISubmitEvaluationRequest(
                                examId,
                                userId,  // participantId
                                examParticipant.getAssignedProblemId(),
                                examParticipant.getSpecId(),
                                request.code(),
                                request.lang(),
                                submission.getId()
                        );

                        aiChatService.submitEvaluation(aiRequest);
                        log.info("AI evaluation request sent after transaction commit: submissionId={}", submission.getId());
                    } catch (Exception e) {
                        log.error("Failed to send AI evaluation request after transaction commit: submissionId={}", 
                                submission.getId(), e);
                        // AI 서버 요청 실패는 로그만 남기고 제출은 성공으로 처리
                    }
                }
            }
        );

        // 5. 응답 반환 (202 Accepted)
        return new SubmitResponse(submission.getId(), submission.getStatus());
    }
}
