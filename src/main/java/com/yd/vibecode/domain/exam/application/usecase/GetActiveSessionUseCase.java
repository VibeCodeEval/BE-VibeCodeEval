package com.yd.vibecode.domain.exam.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.exam.application.dto.response.ActiveSessionResponse;
import com.yd.vibecode.domain.exam.domain.entity.Exam;
import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.exam.domain.service.ExamParticipantService;
import com.yd.vibecode.domain.exam.domain.service.ExamService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.ExamErrorStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetActiveSessionUseCase {

    private final ExamParticipantService examParticipantService;
    private final ExamService examService;

    @Transactional(readOnly = true)
    public ActiveSessionResponse execute(Long participantId) {
        ExamParticipant participant = examParticipantService.findLatestByParticipantId(participantId);
        if (participant == null) {
            throw new RestApiException(ExamErrorStatus.NO_ACTIVE_SESSION);
        }

        Exam exam = examService.findById(participant.getExamId());
        if (!exam.isRunning()) {
            throw new RestApiException(ExamErrorStatus.NO_ACTIVE_SESSION);
        }

        return ActiveSessionResponse.from(exam, participant);
    }

    @Transactional(readOnly = true)
    public ActiveSessionResponse execute(Long examId, Long participantId) {
        Exam exam = examService.findById(examId);
        if (!exam.isRunning()) {
            throw new RestApiException(ExamErrorStatus.NO_ACTIVE_SESSION);
        }

        ExamParticipant participant = examParticipantService.findByExamIdAndParticipantId(examId, participantId);
        if (participant == null) {
            throw new RestApiException(ExamErrorStatus.NO_ACTIVE_SESSION);
        }

        return ActiveSessionResponse.from(exam, participant);
    }
}
