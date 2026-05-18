package com.yd.vibecode.domain.submission.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yd.vibecode.domain.submission.application.dto.response.SubmissionDetailResponse;
import com.yd.vibecode.domain.submission.application.service.SubmissionDetailAssembler;
import com.yd.vibecode.domain.submission.domain.entity.RunGroup;
import com.yd.vibecode.domain.submission.domain.entity.Score;
import com.yd.vibecode.domain.submission.domain.entity.Submission;
import com.yd.vibecode.domain.submission.domain.entity.SubmissionRun;
import com.yd.vibecode.domain.submission.domain.entity.SubmissionStatus;
import com.yd.vibecode.domain.submission.domain.entity.Verdict;
import com.yd.vibecode.domain.submission.domain.repository.ScoreRepository;
import com.yd.vibecode.domain.submission.domain.repository.SubmissionRunRepository;
import com.yd.vibecode.domain.submission.domain.service.SubmissionService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.GlobalErrorStatus;
import com.yd.vibecode.global.exception.code.status.SubmissionErrorStatus;

@ExtendWith(MockitoExtension.class)
class GetSubmissionDetailUseCaseTest {

    @InjectMocks
    private GetSubmissionDetailUseCase getSubmissionDetailUseCase;

    @Mock
    private SubmissionService submissionService;

    @Mock
    private SubmissionRunRepository submissionRunRepository;

    @Mock
    private ScoreRepository scoreRepository;

    @Spy
    private SubmissionDetailAssembler submissionDetailAssembler = new SubmissionDetailAssembler();

    @Test
    @DisplayName("소유자 일치 — 제출 상세 조회 성공")
    void execute_success_whenOwner() {
        Long currentUserId = 100L;
        Long submissionId = 1L;
        Submission submission = Submission.builder()
                .examId(1L)
                .participantId(currentUserId)
                .specId(10L)
                .status(SubmissionStatus.DONE)
                .lang("python3.11")
                .codeLoc(10)
                .build();

        List<SubmissionRun> runs = List.of(
                SubmissionRun.builder().grp(RunGroup.SAMPLE).verdict(Verdict.AC).timeMs(100).memKb(1024).build(),
                SubmissionRun.builder().grp(RunGroup.PUBLIC).verdict(Verdict.AC).timeMs(200).memKb(2048).build(),
                SubmissionRun.builder().grp(RunGroup.PRIVATE).verdict(Verdict.WA).timeMs(150).memKb(1024).build());

        Score score = Score.builder()
                .promptScore(new BigDecimal("30.0"))
                .perfScore(new BigDecimal("20.0"))
                .correctnessScore(new BigDecimal("10.0"))
                .build();
        score.calculateTotalScore();

        given(submissionService.findById(submissionId)).willReturn(submission);
        given(submissionRunRepository.findBySubmissionId(submissionId)).willReturn(runs);
        given(scoreRepository.findBySubmissionId(submissionId)).willReturn(Optional.of(score));

        SubmissionDetailResponse response =
                getSubmissionDetailUseCase.execute(currentUserId, submissionId);

        assertThat(response.status()).isEqualTo(SubmissionStatus.DONE);
        assertThat(response.metrics().timeMsMedian()).isEqualTo(150);
        assertThat(response.metrics().memKbPeak()).isEqualTo(2048);
        assertThat(response.score().total()).isEqualTo(new BigDecimal("60.0"));
        assertThat(response.tc().passRateWeighted()).isEqualTo(0.4);
    }

    @Test
    @DisplayName("소유자 불일치 — 403 (COMMON403)")
    void execute_forbidden_whenNotOwner() {
        Long currentUserId = 100L;
        Long submissionId = 1L;
        Submission submission = Submission.builder()
                .examId(1L)
                .participantId(99L)
                .specId(10L)
                .status(SubmissionStatus.DONE)
                .lang("python3.11")
                .build();

        given(submissionService.findById(submissionId)).willReturn(submission);

        assertThatThrownBy(() -> getSubmissionDetailUseCase.execute(currentUserId, submissionId))
                .isInstanceOf(RestApiException.class)
                .satisfies(ex -> assertThat(((RestApiException) ex).getErrorCode().getCode())
                        .isEqualTo(GlobalErrorStatus._FORBIDDEN.getCode().getCode()));

        verify(submissionRunRepository, never()).findBySubmissionId(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    @DisplayName("존재하지 않는 submissionId — 404 (SUB001)")
    void execute_notFound_submission() {
        Long currentUserId = 100L;
        Long submissionId = 999L;
        given(submissionService.findById(submissionId))
                .willThrow(new RestApiException(SubmissionErrorStatus.SUBMISSION_NOT_FOUND));

        assertThatThrownBy(() -> getSubmissionDetailUseCase.execute(currentUserId, submissionId))
                .isInstanceOf(RestApiException.class)
                .satisfies(ex -> assertThat(((RestApiException) ex).getErrorCode().getCode())
                        .isEqualTo(SubmissionErrorStatus.SUBMISSION_NOT_FOUND.getCode().getCode()));
    }
}
