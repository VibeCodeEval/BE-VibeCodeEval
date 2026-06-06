package com.yd.vibecode.domain.exam.infrastructure;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.yd.vibecode.domain.exam.application.usecase.EndExamUseCase;
import com.yd.vibecode.domain.exam.domain.entity.Exam;
import com.yd.vibecode.domain.exam.domain.entity.ExamState;
import com.yd.vibecode.domain.exam.domain.repository.ExamRepository;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.ExamErrorStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 시험 종료 시각({@code endsAt}) 도래 시 RUNNING 상태 시험을 자동으로 종료한다.
 * <p>
 * 서버 내부 스케줄러 전용이며, 공개 API를 제공하지 않는다.
 * 상태 전환은 {@link EndExamUseCase}에 위임한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExamAutoEndScheduler {

    private static final Set<String> BENIGN_SKIP_ERROR_CODES = Set.of(
            ExamErrorStatus.INVALID_EXAM_STATE.getCode().getCode(),
            ExamErrorStatus.EXAM_ALREADY_ENDED.getCode().getCode()
    );

    private final ExamRepository examRepository;
    private final EndExamUseCase endExamUseCase;

    @Scheduled(fixedDelay = 1_000)
    public void pollAndAutoEndExams() {
        LocalDateTime now = LocalDateTime.now();
        List<Exam> candidates = examRepository.findByStateAndEndsAtIsNotNullAndEndsAtLessThanEqual(
                ExamState.RUNNING,
                now
        );

        if (candidates.isEmpty()) {
            return;
        }

        log.info("[ExamAutoEnd] {} exam(s) eligible (RUNNING, endsAt <= {})", candidates.size(), now);

        int successCount = 0;
        int skipCount = 0;
        int failCount = 0;

        for (Exam exam : candidates) {
            Long examId = exam.getId();
            try {
                endExamUseCase.execute(examId);
                successCount++;
                log.info("[ExamAutoEnd] Success examId={}", examId);
            } catch (RestApiException e) {
                if (isBenignSkip(e)) {
                    skipCount++;
                    log.info(
                            "[ExamAutoEnd] Skipped examId={}, code={}, message={}",
                            examId,
                            e.getErrorCode().getCode(),
                            e.getErrorCode().getMessage()
                    );
                } else {
                    failCount++;
                    log.warn(
                            "[ExamAutoEnd] Failed examId={}, code={}, message={}",
                            examId,
                            e.getErrorCode().getCode(),
                            e.getErrorCode().getMessage()
                    );
                }
            } catch (Exception e) {
                failCount++;
                log.error("[ExamAutoEnd] Failed examId={}", examId, e);
            }
        }

        log.info(
                "[ExamAutoEnd] Batch finished: candidates={}, success={}, skipped={}, failed={}",
                candidates.size(),
                successCount,
                skipCount,
                failCount
        );
    }

    private static boolean isBenignSkip(RestApiException e) {
        String code = e.getErrorCode().getCode();
        return code != null && BENIGN_SKIP_ERROR_CODES.contains(code);
    }
}
