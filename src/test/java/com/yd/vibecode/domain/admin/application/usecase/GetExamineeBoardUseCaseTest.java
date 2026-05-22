package com.yd.vibecode.domain.admin.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.yd.vibecode.domain.admin.application.dto.response.ExamineeBoardResponse;
import com.yd.vibecode.domain.auth.domain.entity.User;
import com.yd.vibecode.domain.auth.domain.repository.UserRepository;
import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.exam.domain.repository.ExamParticipantRepository;
import com.yd.vibecode.domain.submission.domain.entity.Score;
import com.yd.vibecode.domain.submission.domain.entity.Submission;
import com.yd.vibecode.domain.submission.domain.entity.SubmissionStatus;
import com.yd.vibecode.domain.submission.domain.repository.ScoreRepository;
import com.yd.vibecode.domain.submission.domain.repository.SubmissionRepository;

@ExtendWith(MockitoExtension.class)
class GetExamineeBoardUseCaseTest {

    private static final Long EXAM_ID = 1L;

    @Mock
    private ExamParticipantRepository examParticipantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private ScoreRepository scoreRepository;

    @InjectMocks
    private GetExamineeBoardUseCase getExamineeBoardUseCase;

    @Test
    @DisplayName("participantId(users.id) 일치 시 제출 상태·점수 매핑")
    void execute_mapsSubmissionByParticipantId() {
        Long userId = 100L;
        ExamParticipant ep = ExamParticipant.builder()
                .examId(EXAM_ID)
                .participantId(userId)
                .state("RUNNING")
                .build();
        ReflectionTestUtils.setField(ep, "id", 12L);

        User user = User.builder().name("홍길동").phone("010-1234-5678").build();
        ReflectionTestUtils.setField(user, "id", userId);

        Submission submission = Submission.builder()
                .examId(EXAM_ID)
                .participantId(userId)
                .specId(10L)
                .status(SubmissionStatus.DONE)
                .lang("python")
                .build();
        ReflectionTestUtils.setField(submission, "id", 50L);

        Score score = Score.builder()
                .submissionId(50L)
                .promptScore(new BigDecimal("40"))
                .perfScore(new BigDecimal("30"))
                .correctnessScore(new BigDecimal("30"))
                .build();
        score.calculateTotalScore();

        given(examParticipantRepository.findAllByExamId(EXAM_ID)).willReturn(List.of(ep));
        given(userRepository.findAllById(List.of(userId))).willReturn(List.of(user));
        given(submissionRepository.findByExamId(EXAM_ID)).willReturn(List.of(submission));
        given(scoreRepository.findBySubmissionIdIn(List.of(50L))).willReturn(List.of(score));

        List<ExamineeBoardResponse> result = getExamineeBoardUseCase.execute(EXAM_ID);

        assertThat(result).hasSize(1);
        ExamineeBoardResponse row = result.get(0);
        assertThat(row.examParticipantId()).isEqualTo(12L);
        assertThat(row.submitted()).isTrue();
        assertThat(row.submissionId()).isEqualTo(50L);
        assertThat(row.submissionStatus()).isEqualTo("DONE");
        assertThat(row.promptScore()).isEqualByComparingTo(new BigDecimal("40"));
        assertThat(row.perfScore()).isEqualByComparingTo(new BigDecimal("30"));
        assertThat(row.correctnessScore()).isEqualByComparingTo(new BigDecimal("30"));
        assertThat(row.totalScore()).isEqualByComparingTo(new BigDecimal("100"));
    }

    @Test
    @DisplayName("exam_participants.id와 submission.participantId가 우연히 같아도 미제출로 처리")
    void execute_doesNotFallbackToExamParticipantPk() {
        Long userId = 100L;
        Long examParticipantPk = 12L;

        ExamParticipant ep = ExamParticipant.builder()
                .examId(EXAM_ID)
                .participantId(userId)
                .state("RUNNING")
                .build();
        ReflectionTestUtils.setField(ep, "id", examParticipantPk);

        User user = User.builder().name("홍길동").phone("010-1234-5678").build();
        ReflectionTestUtils.setField(user, "id", userId);

        Submission otherUserSubmission = Submission.builder()
                .examId(EXAM_ID)
                .participantId(examParticipantPk)
                .specId(10L)
                .status(SubmissionStatus.DONE)
                .lang("python")
                .build();
        ReflectionTestUtils.setField(otherUserSubmission, "id", 99L);

        given(examParticipantRepository.findAllByExamId(EXAM_ID)).willReturn(List.of(ep));
        given(userRepository.findAllById(List.of(userId))).willReturn(List.of(user));
        given(submissionRepository.findByExamId(EXAM_ID)).willReturn(List.of(otherUserSubmission));
        given(scoreRepository.findBySubmissionIdIn(List.of(99L))).willReturn(List.of());

        List<ExamineeBoardResponse> result = getExamineeBoardUseCase.execute(EXAM_ID);

        assertThat(result).hasSize(1);
        ExamineeBoardResponse row = result.get(0);
        assertThat(row.submitted()).isFalse();
        assertThat(row.submissionId()).isNull();
        assertThat(row.submissionStatus()).isNull();
        assertThat(row.totalScore()).isNull();
    }
}
