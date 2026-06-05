package com.yd.vibecode.domain.exam.infrastructure;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.yd.vibecode.domain.exam.application.usecase.StartExamUseCase;
import com.yd.vibecode.domain.exam.domain.entity.Exam;
import com.yd.vibecode.domain.exam.domain.entity.ExamState;
import com.yd.vibecode.domain.exam.domain.repository.ExamRepository;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.ExamErrorStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 시험 시작 시각({@code startsAt}) 도래 시 WAITING 상태 시험을 자동으로 시작한다.
 * <p>
 * 서버 내부 스케줄러 전용이며, 공개 API를 제공하지 않는다.
 * 상태 전환은 {@link StartExamUseCase}에 위임한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExamAutoStartScheduler {

    private static final Set<String> BENIGN_SKIP_ERROR_CODES = Set.of(
            ExamErrorStatus.INVALID_EXAM_STATE.getCode().getCode(),
            ExamErrorStatus.EXAM_ALREADY_STARTED.getCode().getCode(),
            ExamErrorStatus.EXAM_ALREADY_ENDED.getCode().getCode()
    );

    private final ExamRepository examRepository;
    private final StartExamUseCase startExamUseCase;

    @Scheduled(fixedDelay = 10_000)
    public void pollAndAutoStartExams() {
        LocalDateTime now = LocalDateTime.now();
        List<Exam> candidates = examRepository.findByStateAndStartsAtLessThanEqual(ExamState.WAITING, now);

        if (candidates.isEmpty()) {
            return;
        }

        log.info("[ExamAutoStart] {} exam(s) eligible (WAITING, startsAt <= {})", candidates.size(), now);

        int successCount = 0;
        int skipCount = 0;
        int failCount = 0;

        for (Exam exam : candidates) {
            Long examId = exam.getId();
            try {
                startExamUseCase.execute(examId);
                successCount++;
                log.info("[ExamAutoStart] Success examId={}", examId);
            } catch (RestApiException e) {
                if (isBenignSkip(e)) {
                    skipCount++;
                    log.info(
                            "[ExamAutoStart] Skipped examId={}, code={}, message={}",
                            examId,
                            e.getErrorCode().getCode(),
                            e.getErrorCode().getMessage()
                    );
                } else {
                    failCount++;
                    log.warn(
                            "[ExamAutoStart] Failed examId={}, code={}, message={}",
                            examId,
                            e.getErrorCode().getCode(),
                            e.getErrorCode().getMessage()
                    );
                }
            } catch (Exception e) {
                failCount++;
                log.error("[ExamAutoStart] Failed examId={}", examId, e);
            }
        }

        log.info(
                "[ExamAutoStart] Batch finished: candidates={}, success={}, skipped={}, failed={}",
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
