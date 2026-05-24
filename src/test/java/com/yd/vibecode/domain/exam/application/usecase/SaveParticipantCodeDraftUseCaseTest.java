package com.yd.vibecode.domain.exam.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.yd.vibecode.domain.exam.application.dto.request.SaveParticipantCodeDraftRequest;
import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.exam.domain.service.ExamParticipantService;
import com.yd.vibecode.domain.submission.domain.service.SubmissionService;
import com.yd.vibecode.global.exception.RestApiException;

@ExtendWith(MockitoExtension.class)
class SaveParticipantCodeDraftUseCaseTest {

    private static final Long EXAM_ID = 1L;
    private static final Long USER_ID = 100L;

    @Mock
    private ExamParticipantService examParticipantService;

    @Mock
    private SubmissionService submissionService;

    @InjectMocks
    private SaveParticipantCodeDraftUseCase saveParticipantCodeDraftUseCase;

    @Test
    @DisplayName("제출 전에는 draft 저장 가능")
    void execute_savesDraftWhenNoSubmission() {
        ExamParticipant participant = ExamParticipant.builder()
                .examId(EXAM_ID)
                .participantId(USER_ID)
                .build();
        SaveParticipantCodeDraftRequest request =
                new SaveParticipantCodeDraftRequest("python", "print('draft')");

        given(examParticipantService.findByExamIdAndParticipantId(EXAM_ID, USER_ID)).willReturn(participant);
        given(submissionService.existsByExamIdAndParticipantId(EXAM_ID, USER_ID)).willReturn(false);

        saveParticipantCodeDraftUseCase.execute(EXAM_ID, USER_ID, request);

        assertThat(participant.getLastCodeLang()).isEqualTo("python");
        assertThat(participant.getLastCodeInline()).isEqualTo("print('draft')");
        verify(submissionService).existsByExamIdAndParticipantId(EXAM_ID, USER_ID);
    }

    @Test
    @DisplayName("제출 후 draft PUT은 no-op — clear된 snapshot이 다시 저장되지 않음")
    void execute_noOpWhenSubmissionExists_doesNotRestoreDraft() {
        ExamParticipant participant = ExamParticipant.builder()
                .examId(EXAM_ID)
                .participantId(USER_ID)
                .build();
        ReflectionTestUtils.setField(participant, "updatedAt", LocalDateTime.of(2026, 5, 24, 10, 0));

        SaveParticipantCodeDraftRequest request =
                new SaveParticipantCodeDraftRequest("python", "print('late debounce')");

        given(examParticipantService.findByExamIdAndParticipantId(EXAM_ID, USER_ID)).willReturn(participant);
        given(submissionService.existsByExamIdAndParticipantId(EXAM_ID, USER_ID)).willReturn(true);

        saveParticipantCodeDraftUseCase.execute(EXAM_ID, USER_ID, request);

        assertThat(participant.getLastCodeLang()).isNull();
        assertThat(participant.getLastCodeInline()).isNull();
        assertThat(participant.getUpdatedAt()).isEqualTo(LocalDateTime.of(2026, 5, 24, 10, 0));
    }

    @Test
    @DisplayName("제출 후 draft PUT은 no-op — 기존 snapshot을 덮어쓰지 않음")
    void execute_noOpWhenSubmissionExists_doesNotOverwriteExistingSnapshot() {
        ExamParticipant participant = ExamParticipant.builder()
                .examId(EXAM_ID)
                .participantId(USER_ID)
                .lastCodeLang("python")
                .lastCodeInline("print('old')")
                .build();
        ReflectionTestUtils.setField(participant, "updatedAt", LocalDateTime.of(2026, 5, 24, 11, 0));

        SaveParticipantCodeDraftRequest request =
                new SaveParticipantCodeDraftRequest("java", "System.out.println(1);");

        given(examParticipantService.findByExamIdAndParticipantId(EXAM_ID, USER_ID)).willReturn(participant);
        given(submissionService.existsByExamIdAndParticipantId(EXAM_ID, USER_ID)).willReturn(true);

        saveParticipantCodeDraftUseCase.execute(EXAM_ID, USER_ID, request);

        assertThat(participant.getLastCodeLang()).isEqualTo("python");
        assertThat(participant.getLastCodeInline()).isEqualTo("print('old')");
        assertThat(participant.getUpdatedAt()).isEqualTo(LocalDateTime.of(2026, 5, 24, 11, 0));
    }

    @Test
    @DisplayName("참가자 없으면 NOT_FOUND")
    void execute_throwsWhenParticipantMissing() {
        given(examParticipantService.findByExamIdAndParticipantId(EXAM_ID, USER_ID)).willReturn(null);

        assertThatThrownBy(() -> saveParticipantCodeDraftUseCase.execute(
                EXAM_ID, USER_ID, new SaveParticipantCodeDraftRequest("python", "x")))
                .isInstanceOf(RestApiException.class);

        verify(submissionService, never()).existsByExamIdAndParticipantId(EXAM_ID, USER_ID);
    }
}
