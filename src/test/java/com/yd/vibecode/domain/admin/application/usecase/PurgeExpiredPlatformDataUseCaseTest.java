package com.yd.vibecode.domain.admin.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yd.vibecode.domain.admin.domain.entity.PlatformSettings;
import com.yd.vibecode.domain.admin.domain.repository.AdminActivityLogRepository;
import com.yd.vibecode.domain.admin.domain.repository.MasterActivityLogRepository;
import com.yd.vibecode.domain.chat.domain.repository.PromptEvaluationRepository;
import com.yd.vibecode.domain.chat.domain.repository.PromptMessageRepository;
import com.yd.vibecode.domain.chat.domain.repository.PromptSessionRepository;
import com.yd.vibecode.domain.exam.domain.entity.ExamState;
import com.yd.vibecode.domain.submission.domain.repository.OutboxEventRepository;
import com.yd.vibecode.domain.submission.domain.repository.ScoreRepository;
import com.yd.vibecode.domain.submission.domain.repository.SubmissionRepository;
import com.yd.vibecode.domain.submission.domain.repository.SubmissionRunRepository;

@ExtendWith(MockitoExtension.class)
class PurgeExpiredPlatformDataUseCaseTest {

    @InjectMocks
    private PurgeExpiredPlatformDataUseCase useCase;

    @Mock
    private AdminActivityLogRepository adminActivityLogRepository;

    @Mock
    private MasterActivityLogRepository masterActivityLogRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private SubmissionRunRepository submissionRunRepository;

    @Mock
    private ScoreRepository scoreRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private PromptSessionRepository promptSessionRepository;

    @Mock
    private PromptMessageRepository promptMessageRepository;

    @Mock
    private PromptEvaluationRepository promptEvaluationRepository;

    @Test
    @DisplayName("logRetentionDays 기준으로 admin/master 로그 삭제 repository를 호출한다")
    void execute_deletesLogsByLogRetentionDays() {
        PlatformSettings settings = settings(30, 90);
        given(adminActivityLogRepository.deleteByCreatedAtBefore(any())).willReturn(2);
        given(masterActivityLogRepository.deleteByCreatedAtBefore(any())).willReturn(3);
        given(submissionRepository.findIdsByExamStateAndCreatedAtBefore(eq(ExamState.ENDED), any()))
                .willReturn(List.of());
        given(promptSessionRepository.findIdsByExamStateAndCreatedAtBefore(eq(ExamState.ENDED), any()))
                .willReturn(List.of());

        useCase.execute(settings);

        ArgumentCaptor<LocalDateTime> logCutoffCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(adminActivityLogRepository).deleteByCreatedAtBefore(logCutoffCaptor.capture());
        verify(masterActivityLogRepository).deleteByCreatedAtBefore(logCutoffCaptor.getValue());
    }

    @Test
    @DisplayName("ENDED 시험 제출 산출물을 submissionRetentionDays 기준으로 삭제한다")
    void execute_deletesExpiredEndedExamSubmissionArtifacts() {
        PlatformSettings settings = settings(90, 60);
        List<Long> submissionIds = List.of(10L, 20L);

        given(adminActivityLogRepository.deleteByCreatedAtBefore(any())).willReturn(0);
        given(masterActivityLogRepository.deleteByCreatedAtBefore(any())).willReturn(0);
        given(submissionRepository.findIdsByExamStateAndCreatedAtBefore(eq(ExamState.ENDED), any()))
                .willReturn(submissionIds);
        given(outboxEventRepository.deleteByAggregateTypeAndAggregateIdIn(
                PurgeExpiredPlatformDataUseCase.SUBMISSION_AGGREGATE_TYPE, submissionIds)).willReturn(2);
        given(submissionRunRepository.deleteBySubmissionIdIn(submissionIds)).willReturn(4);
        given(scoreRepository.deleteBySubmissionIdIn(submissionIds)).willReturn(2);
        given(submissionRepository.deleteByIdIn(submissionIds)).willReturn(2);
        given(promptSessionRepository.findIdsByExamStateAndCreatedAtBefore(eq(ExamState.ENDED), any()))
                .willReturn(List.of());

        useCase.execute(settings);

        var inOrder = inOrder(
                outboxEventRepository,
                submissionRunRepository,
                scoreRepository,
                submissionRepository
        );
        inOrder.verify(outboxEventRepository).deleteByAggregateTypeAndAggregateIdIn(
                PurgeExpiredPlatformDataUseCase.SUBMISSION_AGGREGATE_TYPE,
                submissionIds
        );
        inOrder.verify(submissionRunRepository).deleteBySubmissionIdIn(submissionIds);
        inOrder.verify(scoreRepository).deleteBySubmissionIdIn(submissionIds);
        inOrder.verify(submissionRepository).deleteByIdIn(submissionIds);
    }

    @Test
    @DisplayName("제출 id 목록이 비어 있으면 submission 관련 delete query를 호출하지 않는다")
    void execute_skipsSubmissionDeletesWhenNoExpiredSubmissions() {
        PlatformSettings settings = settings(90, 90);
        given(adminActivityLogRepository.deleteByCreatedAtBefore(any())).willReturn(0);
        given(masterActivityLogRepository.deleteByCreatedAtBefore(any())).willReturn(0);
        given(submissionRepository.findIdsByExamStateAndCreatedAtBefore(eq(ExamState.ENDED), any()))
                .willReturn(List.of());
        given(promptSessionRepository.findIdsByExamStateAndCreatedAtBefore(eq(ExamState.ENDED), any()))
                .willReturn(List.of());

        useCase.execute(settings);

        verify(submissionRepository).findIdsByExamStateAndCreatedAtBefore(eq(ExamState.ENDED), any());
        verifyNoInteractions(outboxEventRepository, submissionRunRepository, scoreRepository);
        verify(submissionRepository, never()).deleteByIdIn(any());
    }

    @Test
    @DisplayName("ENDED 시험만 조회하므로 RUNNING/WAITING 시험 제출은 repository 조회 단계에서 제외된다")
    void execute_queriesOnlyEndedExamSubmissions() {
        PlatformSettings settings = settings(90, 90);
        given(adminActivityLogRepository.deleteByCreatedAtBefore(any())).willReturn(0);
        given(masterActivityLogRepository.deleteByCreatedAtBefore(any())).willReturn(0);
        given(submissionRepository.findIdsByExamStateAndCreatedAtBefore(eq(ExamState.ENDED), any()))
                .willReturn(List.of());
        given(promptSessionRepository.findIdsByExamStateAndCreatedAtBefore(eq(ExamState.ENDED), any()))
                .willReturn(List.of());

        useCase.execute(settings);

        verify(submissionRepository).findIdsByExamStateAndCreatedAtBefore(eq(ExamState.ENDED), any());
        verify(submissionRepository, never()).findIdsByExamStateAndCreatedAtBefore(eq(ExamState.RUNNING), any());
        verify(submissionRepository, never()).findIdsByExamStateAndCreatedAtBefore(eq(ExamState.WAITING), any());
    }

    @Test
    @DisplayName("ENDED 시험 prompt 산출물을 submissionRetentionDays 기준으로 삭제한다")
    void execute_deletesExpiredEndedExamPromptArtifacts() {
        PlatformSettings settings = settings(90, 45);
        List<Long> sessionIds = List.of(100L, 200L);

        given(adminActivityLogRepository.deleteByCreatedAtBefore(any())).willReturn(0);
        given(masterActivityLogRepository.deleteByCreatedAtBefore(any())).willReturn(0);
        given(submissionRepository.findIdsByExamStateAndCreatedAtBefore(eq(ExamState.ENDED), any()))
                .willReturn(List.of());
        given(promptSessionRepository.findIdsByExamStateAndCreatedAtBefore(eq(ExamState.ENDED), any()))
                .willReturn(sessionIds);
        given(promptMessageRepository.deleteBySessionIdIn(sessionIds)).willReturn(5);
        given(promptEvaluationRepository.deleteBySessionIdIn(sessionIds)).willReturn(2);
        given(promptSessionRepository.deleteByIdIn(sessionIds)).willReturn(2);

        useCase.execute(settings);

        var inOrder = inOrder(
                promptMessageRepository,
                promptEvaluationRepository,
                promptSessionRepository
        );
        inOrder.verify(promptMessageRepository).deleteBySessionIdIn(sessionIds);
        inOrder.verify(promptEvaluationRepository).deleteBySessionIdIn(sessionIds);
        inOrder.verify(promptSessionRepository).deleteByIdIn(sessionIds);
    }

    @Test
    @DisplayName("prompt session id 목록이 비어 있으면 prompt 관련 delete query를 호출하지 않는다")
    void execute_skipsPromptDeletesWhenNoExpiredSessions() {
        PlatformSettings settings = settings(90, 90);
        given(adminActivityLogRepository.deleteByCreatedAtBefore(any())).willReturn(0);
        given(masterActivityLogRepository.deleteByCreatedAtBefore(any())).willReturn(0);
        given(submissionRepository.findIdsByExamStateAndCreatedAtBefore(eq(ExamState.ENDED), any()))
                .willReturn(List.of());
        given(promptSessionRepository.findIdsByExamStateAndCreatedAtBefore(eq(ExamState.ENDED), any()))
                .willReturn(List.of());

        useCase.execute(settings);

        verify(promptSessionRepository).findIdsByExamStateAndCreatedAtBefore(eq(ExamState.ENDED), any());
        verifyNoInteractions(promptMessageRepository, promptEvaluationRepository);
        verify(promptSessionRepository, never()).deleteByIdIn(any());
    }

    @Test
    @DisplayName("ENDED 시험만 조회하므로 RUNNING/WAITING 시험 prompt는 repository 조회 단계에서 제외된다")
    void execute_queriesOnlyEndedExamPromptSessions() {
        PlatformSettings settings = settings(90, 90);
        given(adminActivityLogRepository.deleteByCreatedAtBefore(any())).willReturn(0);
        given(masterActivityLogRepository.deleteByCreatedAtBefore(any())).willReturn(0);
        given(submissionRepository.findIdsByExamStateAndCreatedAtBefore(eq(ExamState.ENDED), any()))
                .willReturn(List.of());
        given(promptSessionRepository.findIdsByExamStateAndCreatedAtBefore(eq(ExamState.ENDED), any()))
                .willReturn(List.of());

        useCase.execute(settings);

        verify(promptSessionRepository).findIdsByExamStateAndCreatedAtBefore(eq(ExamState.ENDED), any());
        verify(promptSessionRepository, never()).findIdsByExamStateAndCreatedAtBefore(eq(ExamState.RUNNING), any());
        verify(promptSessionRepository, never()).findIdsByExamStateAndCreatedAtBefore(eq(ExamState.WAITING), any());
    }

    private static PlatformSettings settings(int logRetentionDays, int submissionRetentionDays) {
        return PlatformSettings.builder()
                .defaultTokenLimit(10000)
                .logRetentionDays(logRetentionDays)
                .submissionRetentionDays(submissionRetentionDays)
                .autoDeleteExpiredData(true)
                .build();
    }
}
