package com.yd.vibecode.domain.admin.application.usecase;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 플랫폼 전역 보관 정책에 따라 만료된 로그·제출·AI 산출물을 물리 삭제한다.
 * {@code autoDeleteExpiredData=false}이면 스케줄러가 이 UseCase를 호출하지 않는다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PurgeExpiredPlatformDataUseCase {

    static final String SUBMISSION_AGGREGATE_TYPE = "SUBMISSION";

    private final AdminActivityLogRepository adminActivityLogRepository;
    private final MasterActivityLogRepository masterActivityLogRepository;
    private final SubmissionRepository submissionRepository;
    private final SubmissionRunRepository submissionRunRepository;
    private final ScoreRepository scoreRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final PromptSessionRepository promptSessionRepository;
    private final PromptMessageRepository promptMessageRepository;
    private final PromptEvaluationRepository promptEvaluationRepository;

    @Transactional
    public void execute(PlatformSettings settings) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime logCutoff = now.minusDays(settings.getLogRetentionDays());
        LocalDateTime submissionCutoff = now.minusDays(settings.getSubmissionRetentionDays());

        purgeExpiredLogs(logCutoff);
        purgeExpiredSubmissionArtifacts(submissionCutoff);
        purgeExpiredPromptArtifacts(submissionCutoff);
    }

    private void purgeExpiredLogs(LocalDateTime logCutoff) {
        int adminLogsDeleted = adminActivityLogRepository.deleteByCreatedAtBefore(logCutoff);
        int masterLogsDeleted = masterActivityLogRepository.deleteByCreatedAtBefore(logCutoff);
        log.info(
                "[DataRetention] Purged expired logs (cutoff={}): adminActivityLogs={}, masterActivityLogs={}",
                logCutoff,
                adminLogsDeleted,
                masterLogsDeleted
        );
    }

    private void purgeExpiredSubmissionArtifacts(LocalDateTime submissionCutoff) {
        List<Long> submissionIds = submissionRepository.findIdsByExamStateAndCreatedAtBefore(
                ExamState.ENDED,
                submissionCutoff
        );
        if (submissionIds.isEmpty()) {
            log.info(
                    "[DataRetention] No expired submission artifacts to purge (cutoff={}, examState=ENDED)",
                    submissionCutoff
            );
            return;
        }

        int outboxDeleted = outboxEventRepository.deleteByAggregateTypeAndAggregateIdIn(
                SUBMISSION_AGGREGATE_TYPE,
                submissionIds
        );
        int runsDeleted = submissionRunRepository.deleteBySubmissionIdIn(submissionIds);
        int scoresDeleted = scoreRepository.deleteBySubmissionIdIn(submissionIds);
        int submissionsDeleted = submissionRepository.deleteByIdIn(submissionIds);

        log.info(
                "[DataRetention] Purged expired submission artifacts (cutoff={}, examState=ENDED): "
                        + "outboxEvents={}, submissionRuns={}, scores={}, submissions={}",
                submissionCutoff,
                outboxDeleted,
                runsDeleted,
                scoresDeleted,
                submissionsDeleted
        );
    }

    private void purgeExpiredPromptArtifacts(LocalDateTime submissionCutoff) {
        List<Long> sessionIds = promptSessionRepository.findIdsByExamStateAndCreatedAtBefore(
                ExamState.ENDED,
                submissionCutoff
        );
        if (sessionIds.isEmpty()) {
            log.info(
                    "[DataRetention] No expired prompt artifacts to purge (cutoff={}, examState=ENDED)",
                    submissionCutoff
            );
            return;
        }

        int messagesDeleted = promptMessageRepository.deleteBySessionIdIn(sessionIds);
        int evaluationsDeleted = promptEvaluationRepository.deleteBySessionIdIn(sessionIds);
        int sessionsDeleted = promptSessionRepository.deleteByIdIn(sessionIds);

        log.info(
                "[DataRetention] Purged expired prompt artifacts (cutoff={}, examState=ENDED): "
                        + "promptMessages={}, promptEvaluations={}, promptSessions={}",
                submissionCutoff,
                messagesDeleted,
                evaluationsDeleted,
                sessionsDeleted
        );
    }
}
