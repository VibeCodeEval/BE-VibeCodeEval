package com.yd.vibecode.domain.submission.domain.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.chat.domain.service.PromptSessionService;
import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.exam.domain.service.ExamParticipantService;
import com.yd.vibecode.domain.problem.domain.entity.ProblemSpec;
import com.yd.vibecode.domain.problem.domain.service.ProblemSpecService;
import com.yd.vibecode.domain.submission.application.dto.request.AISubmitEvaluationRequest;
import com.yd.vibecode.domain.submission.application.dto.request.SubmitRequest;
import com.yd.vibecode.domain.submission.application.dto.response.SubmitResponse;
import com.yd.vibecode.domain.submission.domain.entity.Submission;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.ProblemErrorStatus;
import com.yd.vibecode.global.exception.code.status.SubmissionErrorStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 참가자 제출 생성 + 채점/AI 평가 요청 공통 오케스트레이션.
 * 사용자 직접 제출과 시험 종료 시 자동 제출이 동일 경로를 사용한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ParticipantSubmitOrchestrationService {

    private final ExamParticipantService examParticipantService;
    private final SubmissionService submissionService;
    private final ProblemSpecService problemSpecService;
    private final PromptSessionService promptSessionService;
    private final OutboxEventService outboxEventService;

    /**
     * @return 제출이 새로 생성되면 SubmitResponse, 이미 제출된 경우 empty
     */
    @Transactional
    public java.util.Optional<SubmitResponse> submitIfAbsent(Long examId, Long participantId, SubmitRequest request) {
        ExamParticipant examParticipant =
                examParticipantService.findByExamIdAndParticipantIdForUpdate(examId, participantId);

        if (examParticipant == null || examParticipant.getSpecId() == null) {
            throw new RestApiException(ProblemErrorStatus.NO_ASSIGNED_PROBLEM);
        }

        if (submissionService.existsByExamIdAndParticipantId(examId, participantId)) {
            return java.util.Optional.empty();
        }

        Long problemId = examParticipant.getAssignedProblemId();
        if (problemId == null) {
            ProblemSpec spec = problemSpecService.findBySpecId(examParticipant.getSpecId());
            problemId = spec.getProblemId();
        }

        Submission submission = submissionService.createAndEnqueue(
                examId,
                participantId,
                examParticipant.getSpecId(),
                request.lang(),
                request.code()
        );

        promptSessionService.getOrCreateSession(examId, participantId, examParticipant.getSpecId());

        AISubmitEvaluationRequest aiRequest = new AISubmitEvaluationRequest(
                examId,
                participantId,
                problemId,
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

        examParticipant.clearCodeSnapshot();

        log.info("Participant submission created: examId={}, participantId={}, submissionId={}",
                examId, participantId, submission.getId());

        return java.util.Optional.of(new SubmitResponse(submission.getId(), submission.getStatus()));
    }

    @Transactional
    public SubmitResponse submitOrThrow(Long examId, Long participantId, SubmitRequest request) {
        return submitIfAbsent(examId, participantId, request)
                .orElseThrow(() -> new RestApiException(SubmissionErrorStatus.ALREADY_SUBMITTED));
    }
}
