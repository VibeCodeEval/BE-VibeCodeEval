package com.yd.vibecode.domain.exam.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.exam.domain.repository.ExamParticipantRepository;
import com.yd.vibecode.domain.submission.application.dto.request.SubmitRequest;
import com.yd.vibecode.domain.submission.application.dto.response.SubmitResponse;
import com.yd.vibecode.domain.submission.domain.entity.SubmissionStatus;
import com.yd.vibecode.domain.submission.domain.service.ParticipantSubmitOrchestrationService;
import com.yd.vibecode.domain.submission.domain.service.SubmissionService;

@ExtendWith(MockitoExtension.class)
class AutoSubmitParticipantsOnExamEndUseCaseTest {

    private static final Long EXAM_ID = 1L;

    @Mock
    private ExamParticipantRepository examParticipantRepository;

    @Mock
    private SubmissionService submissionService;

    @Mock
    private ParticipantSubmitOrchestrationService participantSubmitOrchestrationService;

    @InjectMocks
    private AutoSubmitParticipantsOnExamEndUseCase autoSubmitParticipantsOnExamEndUseCase;

    @Test
    @DisplayName("미제출 + 코드 스냅샷 있으면 자동 제출")
    void execute_autoSubmitsWhenCodeSnapshotExists() {
        Long participantId = 100L;
        ExamParticipant ep = ExamParticipant.builder()
                .examId(EXAM_ID)
                .participantId(participantId)
                .lastCodeLang("python")
                .lastCodeInline("print(1)")
                .build();

        given(examParticipantRepository.findAllByExamId(EXAM_ID)).willReturn(List.of(ep));
        given(submissionService.existsByExamIdAndParticipantId(EXAM_ID, participantId)).willReturn(false);
        given(participantSubmitOrchestrationService.submitIfAbsent(
                eq(EXAM_ID), eq(participantId), any(SubmitRequest.class)))
                .willReturn(Optional.of(new SubmitResponse(50L, SubmissionStatus.QUEUED)));

        AutoSubmitParticipantsOnExamEndUseCase.AutoSubmitResult result =
                autoSubmitParticipantsOnExamEndUseCase.execute(EXAM_ID);

        assertThat(result.submittedCount()).isEqualTo(1);
        assertThat(result.skippedAlreadySubmitted()).isZero();
        assertThat(result.skippedNoCodeSnapshot()).isZero();
        verify(participantSubmitOrchestrationService).submitIfAbsent(
                eq(EXAM_ID), eq(participantId), any(SubmitRequest.class));
    }

    @Test
    @DisplayName("이미 제출된 참가자는 중복 제출하지 않음")
    void execute_skipsAlreadySubmitted() {
        Long participantId = 100L;
        ExamParticipant ep = ExamParticipant.builder()
                .examId(EXAM_ID)
                .participantId(participantId)
                .lastCodeLang("python")
                .lastCodeInline("print(1)")
                .build();

        given(examParticipantRepository.findAllByExamId(EXAM_ID)).willReturn(List.of(ep));
        given(submissionService.existsByExamIdAndParticipantId(EXAM_ID, participantId)).willReturn(true);

        AutoSubmitParticipantsOnExamEndUseCase.AutoSubmitResult result =
                autoSubmitParticipantsOnExamEndUseCase.execute(EXAM_ID);

        assertThat(result.submittedCount()).isZero();
        assertThat(result.skippedAlreadySubmitted()).isEqualTo(1);
        verify(participantSubmitOrchestrationService, never()).submitIfAbsent(any(), any(), any());
    }

    @Test
    @DisplayName("코드 스냅샷 없으면 자동 제출 스킵")
    void execute_skipsWhenNoCodeSnapshot() {
        Long participantId = 100L;
        ExamParticipant ep = ExamParticipant.builder()
                .examId(EXAM_ID)
                .participantId(participantId)
                .build();

        given(examParticipantRepository.findAllByExamId(EXAM_ID)).willReturn(List.of(ep));
        given(submissionService.existsByExamIdAndParticipantId(EXAM_ID, participantId)).willReturn(false);

        AutoSubmitParticipantsOnExamEndUseCase.AutoSubmitResult result =
                autoSubmitParticipantsOnExamEndUseCase.execute(EXAM_ID);

        assertThat(result.submittedCount()).isZero();
        assertThat(result.skippedNoCodeSnapshot()).isEqualTo(1);
        verify(participantSubmitOrchestrationService, never()).submitIfAbsent(any(), any(), any());
    }
}
