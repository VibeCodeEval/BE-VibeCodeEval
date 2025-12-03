package com.yd.vibecode.domain.submission.application.usecase;

import com.yd.vibecode.domain.submission.application.dto.response.SubmissionDetailResponse;
import com.yd.vibecode.domain.submission.domain.entity.RunGroup;
import com.yd.vibecode.domain.submission.domain.entity.Score;
import com.yd.vibecode.domain.submission.domain.entity.Submission;
import com.yd.vibecode.domain.submission.domain.entity.SubmissionRun;
import com.yd.vibecode.domain.submission.domain.entity.SubmissionStatus;
import com.yd.vibecode.domain.submission.domain.entity.Verdict;
import com.yd.vibecode.domain.submission.domain.repository.ScoreRepository;
import com.yd.vibecode.domain.submission.domain.repository.SubmissionRunRepository;
import com.yd.vibecode.domain.submission.domain.service.SubmissionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

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

    @Test
    @DisplayName("제출 상세 조회 성공: 메트릭 및 점수 계산 확인")
    void execute_Success() {
        // given
        Long submissionId = 1L;
        Submission submission = Submission.builder()
                .status(SubmissionStatus.DONE)
                .lang("python3.11")
                .codeLoc(10)
                .build();

        List<SubmissionRun> runs = List.of(
            SubmissionRun.builder().grp(RunGroup.SAMPLE).verdict(Verdict.AC).timeMs(100).memKb(1024).build(),
            SubmissionRun.builder().grp(RunGroup.PUBLIC).verdict(Verdict.AC).timeMs(200).memKb(2048).build(),
            SubmissionRun.builder().grp(RunGroup.PRIVATE).verdict(Verdict.WA).timeMs(150).memKb(1024).build()
        );

        Score score = Score.builder()
                .promptScore(new BigDecimal("30.0"))
                .perfScore(new BigDecimal("20.0"))
                .correctnessScore(new BigDecimal("10.0"))
                .build();
        score.calculateTotalScore();

        given(submissionService.findById(submissionId)).willReturn(submission);
        given(submissionRunRepository.findBySubmissionId(submissionId)).willReturn(runs);
        given(scoreRepository.findBySubmissionId(submissionId)).willReturn(Optional.of(score));

        // when
        SubmissionDetailResponse response = getSubmissionDetailUseCase.execute(submissionId);

        // then
        assertThat(response.status()).isEqualTo(SubmissionStatus.DONE);
        assertThat(response.metrics().timeMsMedian()).isEqualTo(150); // Median of 100, 150, 200 is 150
        assertThat(response.metrics().memKbPeak()).isEqualTo(2048);
        assertThat(response.score().total()).isEqualTo(new BigDecimal("60.0"));
        
        // Check test case groups
        // SAMPLE: 1/1 pass
        // PUBLIC: 1/1 pass
        // PRIVATE: 0/1 pass
        // Weights: SAMPLE 0.1, PUBLIC 0.3, PRIVATE 0.6
        // Weighted Pass Rate: (1.0 * 0.1 + 1.0 * 0.3 + 0.0 * 0.6) / 1.0 = 0.4
        assertThat(response.tc().passRateWeighted()).isEqualTo(0.4);
    }
}
