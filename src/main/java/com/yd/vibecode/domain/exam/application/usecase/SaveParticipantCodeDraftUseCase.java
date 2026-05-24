package com.yd.vibecode.domain.exam.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.exam.application.dto.request.SaveParticipantCodeDraftRequest;
import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.exam.domain.service.ExamParticipantService;
import com.yd.vibecode.domain.submission.domain.service.SubmissionService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.GlobalErrorStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SaveParticipantCodeDraftUseCase {

    private final ExamParticipantService examParticipantService;
    private final SubmissionService submissionService;

    @Transactional
    public void execute(Long examId, Long participantId, SaveParticipantCodeDraftRequest request) {
        ExamParticipant participant = examParticipantService.findByExamIdAndParticipantId(examId, participantId);
        if (participant == null) {
            throw new RestApiException(GlobalErrorStatus._NOT_FOUND);
        }

        if (submissionService.existsByExamIdAndParticipantId(examId, participantId)) {
            return;
        }

        participant.updateCodeSnapshot(request.lang(), request.code());
    }
}
