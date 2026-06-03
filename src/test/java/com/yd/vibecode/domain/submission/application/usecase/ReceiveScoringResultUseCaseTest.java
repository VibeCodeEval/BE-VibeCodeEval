package com.yd.vibecode.domain.submission.application.usecase;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.yd.vibecode.domain.auth.domain.entity.User;
import com.yd.vibecode.domain.exam.domain.entity.Exam;
import com.yd.vibecode.domain.exam.domain.entity.ExamState;
import com.yd.vibecode.domain.submission.application.dto.request.ScoringResultRequest;
import com.yd.vibecode.domain.submission.application.event.ScoringResultSseEvent;
import com.yd.vibecode.domain.submission.domain.entity.Score;
import com.yd.vibecode.domain.submission.domain.entity.Submission;
import com.yd.vibecode.domain.submission.domain.entity.SubmissionRun;
import com.yd.vibecode.domain.submission.domain.entity.SubmissionStatus;
import com.yd.vibecode.domain.submission.domain.entity.Verdict;
import com.yd.vibecode.domain.submission.domain.repository.ScoreRepository;
import com.yd.vibecode.domain.submission.domain.repository.SubmissionRunRepository;
import com.yd.vibecode.domain.submission.domain.service.SubmissionService;
import com.yd.vibecode.global.exception.RestApiException;

import java.time.LocalDateTime;

import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ReceiveScoringResultUseCaseTest {

    @InjectMocks
    private ReceiveScoringResultUseCase receiveScoringResultUseCase;

    @Mock
    private SubmissionService submissionService;

    @Mock
    private SubmissionRunRepository submissionRunRepository;

    @Mock
    private ScoreRepository scoreRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private com.yd.vibecode.domain.exam.domain.repository.ExamRepository examRepository;

    @Mock
    private com.yd.vibecode.domain.admin.domain.service.AdminActivityLogService adminActivityLogService;

    @Mock
    private com.yd.vibecode.domain.auth.domain.repository.UserRepository userRepository;

    @Test
    @DisplayName("채점 결과 수신 및 처리 성공 - DB 저장 및 SSE 이벤트 발행 확인")
    void execute_Success() {
        Long submissionId = 1L;
        Submission submission = Submission.builder()
                .status(SubmissionStatus.RUNNING)
                .build();

        ScoringResultRequest.TestCaseResult testCase = new ScoringResultRequest.TestCaseResult(
                0, "SAMPLE", Verdict.AC, 100, 1024, 0, 0);

        ScoringResultRequest.ScoreData scoreResult = new ScoringResultRequest.ScoreData(
                new BigDecimal("30.0"), new BigDecimal("30.0"), new BigDecimal("40.0"), "{}");

        ScoringResultRequest request = new ScoringResultRequest(
                SubmissionStatus.DONE,
                List.of(testCase),
                scoreResult);

        given(submissionService.findById(submissionId)).willReturn(submission);
        given(scoreRepository.findBySubmissionId(submissionId)).willReturn(Optional.empty());

        receiveScoringResultUseCase.execute(submissionId, request);

        verify(submissionRunRepository).deleteBySubmissionId(submissionId);
        verify(submissionRunRepository).save(any(SubmissionRun.class));
        verify(scoreRepository).save(any(Score.class));
        verify(eventPublisher).publishEvent(any(ScoringResultSseEvent.class));
    }

    @Test
    @DisplayName("DONE 상태인데 testCases가 비어 있으면 400")
    void execute_doneWithEmptyTestCases_throwsBadRequest() {
        Long submissionId = 1L;
        ScoringResultRequest request = new ScoringResultRequest(
                SubmissionStatus.DONE,
                Collections.emptyList(),
                null);

        assertThatThrownBy(() -> receiveScoringResultUseCase.execute(submissionId, request))
                .isInstanceOf(RestApiException.class);
    }

    @Test
    @DisplayName("잘못된 group 값이면 400")
    void execute_invalidGroup_throwsBadRequest() {
        Long submissionId = 1L;
        Submission submission = Submission.builder().build();
        ScoringResultRequest.TestCaseResult testCase = new ScoringResultRequest.TestCaseResult(
                0, "INVALID", Verdict.AC, 100, 1024, 0, 0);
        ScoringResultRequest request = new ScoringResultRequest(
                SubmissionStatus.DONE,
                List.of(testCase),
                null);

        given(submissionService.findById(submissionId)).willReturn(submission);

        assertThatThrownBy(() -> receiveScoringResultUseCase.execute(submissionId, request))
                .isInstanceOf(RestApiException.class);
    }

    @Test
    @DisplayName("FAILED + 빈 testCases는 허용")
    void execute_failedWithEmptyTestCases_succeeds() {
        Long submissionId = 1L;
        Submission submission = Submission.builder().build();
        ScoringResultRequest request = new ScoringResultRequest(
                SubmissionStatus.FAILED,
                Collections.emptyList(),
                null);

        given(submissionService.findById(submissionId)).willReturn(submission);

        receiveScoringResultUseCase.execute(submissionId, request);

        verify(submissionRunRepository).deleteBySubmissionId(submissionId);
        verify(eventPublisher).publishEvent(any(ScoringResultSseEvent.class));
    }

    @Test
    @DisplayName("재콜백 시 기존 runs 삭제 후 저장")
    void execute_replace_deletesBeforeEachSave() {
        Long submissionId = 1L;
        Submission submission = Submission.builder().build();
        ScoringResultRequest.TestCaseResult testCase = new ScoringResultRequest.TestCaseResult(
                0, "PUBLIC", Verdict.WA, 50, 512, 0, 0);
        ScoringResultRequest request = new ScoringResultRequest(
                SubmissionStatus.DONE,
                List.of(testCase),
                null);

        given(submissionService.findById(submissionId)).willReturn(submission);

        receiveScoringResultUseCase.execute(submissionId, request);
        receiveScoringResultUseCase.execute(submissionId, request);

        verify(submissionRunRepository, times(2)).deleteBySubmissionId(submissionId);
        verify(submissionRunRepository, times(2)).save(any(SubmissionRun.class));
    }

    @Test
    @DisplayName("기존 score가 있으면 upsert")
    void execute_existingScore_updatesInsteadOfDuplicateInsert() {
        Long submissionId = 1L;
        Submission submission = Submission.builder().build();
        Score existing = Score.builder()
                .submissionId(submissionId)
                .promptScore(BigDecimal.ZERO)
                .perfScore(BigDecimal.ZERO)
                .correctnessScore(BigDecimal.ZERO)
                .build();

        ScoringResultRequest request = new ScoringResultRequest(
                SubmissionStatus.DONE,
                List.of(new ScoringResultRequest.TestCaseResult(
                        0, "SAMPLE", Verdict.AC, 10, 100, 0, 0)),
                new ScoringResultRequest.ScoreData(
                        new BigDecimal("10"), new BigDecimal("20"), new BigDecimal("30"), "{}"));

        given(submissionService.findById(submissionId)).willReturn(submission);
        given(scoreRepository.findBySubmissionId(submissionId)).willReturn(Optional.of(existing));

        receiveScoringResultUseCase.execute(submissionId, request);

        verify(scoreRepository).save(existing);
    }

    @Test
    @DisplayName("RUNNING → DONE 최초 전환 시 평가 완료 로그 기록")
    void execute_logsEvaluationCompletedOnFirstTransitionToDone() {
        Long submissionId = 1L;
        Long examId = 100L;
        Long participantId = 200L;
        Long adminId = 10L;

        Submission submission = Submission.builder()
                .examId(examId)
                .participantId(participantId)
                .status(SubmissionStatus.RUNNING)
                .build();
        Exam exam = Exam.builder()
                .title("기말 시험")
                .state(ExamState.RUNNING)
                .startsAt(LocalDateTime.now())
                .endsAt(LocalDateTime.now().plusHours(2))
                .createdBy(adminId)
                .build();
        ReflectionTestUtils.setField(exam, "id", examId);
        User user = User.builder().name("김민준").phone("01012345678").build();

        ScoringResultRequest request = doneRequestWithScore();

        given(submissionService.findById(submissionId)).willReturn(submission);
        given(scoreRepository.findBySubmissionId(submissionId)).willReturn(Optional.empty());
        given(examRepository.findById(examId)).willReturn(Optional.of(exam));
        given(userRepository.findById(participantId)).willReturn(Optional.of(user));

        receiveScoringResultUseCase.execute(submissionId, request);

        verify(adminActivityLogService).logEvaluationCompleted(
                eq(adminId), eq(examId), eq(participantId), eq("기말 시험"), eq("김민준"));
    }

    @Test
    @DisplayName("이미 DONE 상태인 제출에 DONE 재콜백 시 평가 완료 로그 미기록")
    void execute_duplicateDoneCallback_doesNotLogEvaluationCompleted() {
        Long submissionId = 1L;
        Long examId = 100L;
        Long participantId = 200L;

        Submission submission = Submission.builder()
                .examId(examId)
                .participantId(participantId)
                .status(SubmissionStatus.DONE)
                .build();

        ScoringResultRequest request = doneRequestWithScore();

        given(submissionService.findById(submissionId)).willReturn(submission);
        given(scoreRepository.findBySubmissionId(submissionId)).willReturn(Optional.empty());

        receiveScoringResultUseCase.execute(submissionId, request);
        receiveScoringResultUseCase.execute(submissionId, request);

        verify(adminActivityLogService, never()).logEvaluationCompleted(any(), any(), any(), any(), any());
        verify(examRepository, never()).findById(any());
    }

    @Test
    @DisplayName("DONE이 아닌 상태 요청 시 평가 완료 로그 미기록")
    void execute_nonDoneStatus_doesNotLogEvaluationCompleted() {
        Long submissionId = 1L;
        Submission submission = Submission.builder()
                .examId(100L)
                .participantId(200L)
                .status(SubmissionStatus.RUNNING)
                .build();

        ScoringResultRequest request = new ScoringResultRequest(
                SubmissionStatus.FAILED,
                Collections.emptyList(),
                new ScoringResultRequest.ScoreData(
                        new BigDecimal("10"), new BigDecimal("20"), new BigDecimal("30"), "{}"));

        given(submissionService.findById(submissionId)).willReturn(submission);

        receiveScoringResultUseCase.execute(submissionId, request);

        verify(adminActivityLogService, never()).logEvaluationCompleted(any(), any(), any(), any(), any());
        verify(examRepository, never()).findById(any());
    }

    @Test
    @DisplayName("RUNNING → DONE 후 재콜백 시 평가 완료 로그는 1회만 기록")
    void execute_runningToDoneThenDuplicateCallback_logsOnce() {
        Long submissionId = 1L;
        Long examId = 100L;
        Long participantId = 200L;
        Long adminId = 10L;

        Submission submission = Submission.builder()
                .examId(examId)
                .participantId(participantId)
                .status(SubmissionStatus.RUNNING)
                .build();
        Exam exam = Exam.builder()
                .title("기말 시험")
                .state(ExamState.RUNNING)
                .startsAt(LocalDateTime.now())
                .endsAt(LocalDateTime.now().plusHours(2))
                .createdBy(adminId)
                .build();
        ReflectionTestUtils.setField(exam, "id", examId);

        ScoringResultRequest request = doneRequestWithScore();

        given(submissionService.findById(submissionId)).willReturn(submission);
        given(scoreRepository.findBySubmissionId(submissionId)).willReturn(Optional.empty());
        given(examRepository.findById(examId)).willReturn(Optional.of(exam));
        given(userRepository.findById(participantId)).willReturn(Optional.empty());

        receiveScoringResultUseCase.execute(submissionId, request);
        receiveScoringResultUseCase.execute(submissionId, request);

        verify(adminActivityLogService, times(1)).logEvaluationCompleted(
                eq(adminId), eq(examId), eq(participantId), eq("기말 시험"), eq(null));
    }

    private static ScoringResultRequest doneRequestWithScore() {
        return new ScoringResultRequest(
                SubmissionStatus.DONE,
                List.of(new ScoringResultRequest.TestCaseResult(
                        0, "SAMPLE", Verdict.AC, 100, 1024, 0, 0)),
                new ScoringResultRequest.ScoreData(
                        new BigDecimal("30.0"), new BigDecimal("30.0"), new BigDecimal("40.0"), "{}"));
    }
}
