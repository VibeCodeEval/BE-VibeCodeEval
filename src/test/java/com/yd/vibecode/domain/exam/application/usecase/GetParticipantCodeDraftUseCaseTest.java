package com.yd.vibecode.domain.exam.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.yd.vibecode.domain.exam.application.dto.response.CodeDraftResponse;
import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.exam.domain.service.ExamParticipantService;
import com.yd.vibecode.domain.submission.domain.service.SubmissionService;
import com.yd.vibecode.global.exception.RestApiException;

@ExtendWith(MockitoExtension.class)
class GetParticipantCodeDraftUseCaseTest {

    private static final Long EXAM_ID = 1L;
    private static final Long USER_ID = 100L;

    @Mock
    private ExamParticipantService examParticipantService;

    @Mock
    private SubmissionService submissionService;

    @InjectMocks
    private GetParticipantCodeDraftUseCase getParticipantCodeDraftUseCase;

    @Test
    @DisplayName("draft가 있으면 language/codeInline/savedAt 반환")
    void execute_returnsDraftWhenSnapshotExists() {
        ExamParticipant participant = ExamParticipant.builder()
                .examId(EXAM_ID)
                .participantId(USER_ID)
                .lastCodeLang("python")
                .lastCodeInline("print(1)")
                .build();
        ReflectionTestUtils.setField(participant, "updatedAt", LocalDateTime.of(2026, 5, 24, 12, 0));

        given(examParticipantService.findByExamIdAndParticipantId(EXAM_ID, USER_ID)).willReturn(participant);
        given(submissionService.existsByExamIdAndParticipantId(EXAM_ID, USER_ID)).willReturn(false);

        CodeDraftResponse response = getParticipantCodeDraftUseCase.execute(EXAM_ID, USER_ID);

        assertThat(response).isNotNull();
        assertThat(response.language()).isEqualTo("python");
        assertThat(response.codeInline()).isEqualTo("print(1)");
        assertThat(response.savedAt()).isEqualTo(LocalDateTime.of(2026, 5, 24, 12, 0));
    }

    @Test
    @DisplayName("draft 없으면 null")
    void execute_returnsNullWhenNoSnapshot() {
        ExamParticipant participant = ExamParticipant.builder()
                .examId(EXAM_ID)
                .participantId(USER_ID)
                .build();

        given(examParticipantService.findByExamIdAndParticipantId(EXAM_ID, USER_ID)).willReturn(participant);
        given(submissionService.existsByExamIdAndParticipantId(EXAM_ID, USER_ID)).willReturn(false);

        assertThat(getParticipantCodeDraftUseCase.execute(EXAM_ID, USER_ID)).isNull();
    }

    @Test
    @DisplayName("이미 제출된 참가자는 null")
    void execute_returnsNullWhenAlreadySubmitted() {
        ExamParticipant participant = ExamParticipant.builder()
                .examId(EXAM_ID)
                .participantId(USER_ID)
                .lastCodeLang("python")
                .lastCodeInline("print(1)")
                .build();

        given(examParticipantService.findByExamIdAndParticipantId(EXAM_ID, USER_ID)).willReturn(participant);
        given(submissionService.existsByExamIdAndParticipantId(EXAM_ID, USER_ID)).willReturn(true);

        assertThat(getParticipantCodeDraftUseCase.execute(EXAM_ID, USER_ID)).isNull();
    }

    @Test
    @DisplayName("참가자 없으면 NOT_FOUND")
    void execute_throwsWhenParticipantMissing() {
        given(examParticipantService.findByExamIdAndParticipantId(EXAM_ID, USER_ID)).willReturn(null);

        assertThatThrownBy(() -> getParticipantCodeDraftUseCase.execute(EXAM_ID, USER_ID))
                .isInstanceOf(RestApiException.class);
    }
}
