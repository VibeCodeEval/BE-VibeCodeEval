package com.yd.vibecode.domain.submission.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

/**
 * 제출 UseCase
 * - 코드 검증 및 해시 계산
 * - DB 저장
 * - Redis Queue에 enqueue (채점 워커가 dequeue하여 처리)
 */
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

        // 3. AI 평가 요청 (비동기 Callback 유도)
        // 세션은 callback 시 submissionId로 역추적하므로 여기서는 생성만 함
        promptSessionService.getOrCreateSession(
                examId, userId, examParticipant.getSpecId());

        AISubmitEvaluationRequest aiRequest = new AISubmitEvaluationRequest(
                userId,  // participantId 추가
                examParticipant.getAssignedProblemId(),
                examParticipant.getSpecId(),
                request.code(),
                request.lang(),
                submission.getId()
        );

        aiChatService.submitEvaluation(aiRequest);

        // 4. 응답 반환 (202 Accepted)
        return new SubmitResponse(submission.getId(), submission.getStatus());
    }
}
