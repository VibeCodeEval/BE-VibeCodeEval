package com.yd.vibecode.domain.exam.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.exam.application.dto.response.CodeDraftResponse;
import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.exam.domain.service.ExamParticipantService;
import com.yd.vibecode.domain.submission.domain.service.SubmissionService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.GlobalErrorStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetParticipantCodeDraftUseCase {

    private final ExamParticipantService examParticipantService;
    private final SubmissionService submissionService;

    @Transactional(readOnly = true)
    public CodeDraftResponse execute(Long examId, Long participantId) {
        ExamParticipant participant = examParticipantService.findByExamIdAndParticipantId(examId, participantId);
        if (participant == null) {
            throw new RestApiException(GlobalErrorStatus._NOT_FOUND);
        }

        if (submissionService.existsByExamIdAndParticipantId(examId, participantId)) {
            return null;
        }

        if (!participant.hasCodeSnapshot()) {
            return null;
        }

        return new CodeDraftResponse(
                participant.getLastCodeLang(),
                participant.getLastCodeInline(),
                participant.getUpdatedAt());
    }
}
