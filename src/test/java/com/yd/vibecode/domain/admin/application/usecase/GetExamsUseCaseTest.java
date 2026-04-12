package com.yd.vibecode.domain.admin.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yd.vibecode.domain.exam.application.dto.response.ExamResponse;
import com.yd.vibecode.domain.exam.domain.entity.Exam;
import com.yd.vibecode.domain.exam.domain.entity.ExamState;
import com.yd.vibecode.domain.exam.domain.repository.ExamParticipantRepository;
import com.yd.vibecode.domain.exam.domain.repository.ExamRepository;
import com.yd.vibecode.domain.submission.domain.repository.SubmissionRepository;

@ExtendWith(MockitoExtension.class)
class GetExamsUseCaseTest {

    @Mock
    private ExamRepository examRepository;
    @Mock
    private ExamParticipantRepository examParticipantRepository;
    @Mock
    private SubmissionRepository submissionRepository;

    @InjectMocks
    private GetExamsUseCase getExamsUseCase;

    @Test
    @DisplayName("모든 시험 조회 성공")
    void execute_success() {
        // given
        Exam exam1 = Exam.builder()
            .title("Test Exam 1")
            .state(ExamState.WAITING)
            .startsAt(LocalDateTime.now().plusHours(1))
            .endsAt(LocalDateTime.now().plusHours(3))
            .version(0)
            .createdBy(1L)
            .build();
        org.springframework.test.util.ReflectionTestUtils.setField(exam1, "id", 1L);

        Exam exam2 = Exam.builder()
            .title("Test Exam 2")
            .state(ExamState.RUNNING)
            .startsAt(LocalDateTime.now().minusHours(1))
            .endsAt(LocalDateTime.now().plusHours(1))
            .version(1)
            .createdBy(1L)
            .build();
        org.springframework.test.util.ReflectionTestUtils.setField(exam2, "id", 2L);

        given(examRepository.findAll())
            .willReturn(List.of(exam1, exam2));
        given(examParticipantRepository.countByExamId(1L)).willReturn(3L);
        given(examParticipantRepository.countByExamId(2L)).willReturn(5L);
        given(submissionRepository.countByExamId(1L)).willReturn(1L);
        given(submissionRepository.countByExamId(2L)).willReturn(4L);

        // when
        List<ExamResponse> result = getExamsUseCase.execute();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).title()).isEqualTo("Test Exam 1");
        assertThat(result.get(0).state()).isEqualTo(ExamState.WAITING);
        assertThat(result.get(0).participantCount()).isEqualTo(3L);
        assertThat(result.get(0).completedCount()).isEqualTo(1L);
        assertThat(result.get(1).title()).isEqualTo("Test Exam 2");
        assertThat(result.get(1).state()).isEqualTo(ExamState.RUNNING);
        assertThat(result.get(1).participantCount()).isEqualTo(5L);
        assertThat(result.get(1).completedCount()).isEqualTo(4L);
        verify(examRepository).findAll();
        verify(examParticipantRepository).countByExamId(1L);
        verify(examParticipantRepository).countByExamId(2L);
        verify(submissionRepository).countByExamId(1L);
        verify(submissionRepository).countByExamId(2L);
    }

    @Test
    @DisplayName("시험이 없을 때 빈 리스트 반환")
    void execute_noExams_returnsEmptyList() {
        // given
        given(examRepository.findAll())
            .willReturn(List.of());

        // when
        List<ExamResponse> result = getExamsUseCase.execute();

        // then
        assertThat(result).isEmpty();
    }
}
