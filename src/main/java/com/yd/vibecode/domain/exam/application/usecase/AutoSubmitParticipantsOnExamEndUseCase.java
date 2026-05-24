package com.yd.vibecode.domain.exam.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.exam.domain.repository.ExamParticipantRepository;
import com.yd.vibecode.domain.submission.application.dto.request.SubmitRequest;
import com.yd.vibecode.domain.submission.domain.service.ParticipantSubmitOrchestrationService;
import com.yd.vibecode.domain.submission.domain.service.SubmissionService;
import com.yd.vibecode.global.exception.RestApiException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 시험 종료 시 미제출 참가자 자동 제출.
 * 저장된 코드 스냅샷(lastCodeLang/lastCodeInline)이 있을 때만 제출한다.
 *
 * <p>TODO: 접속자·관리자가 없을 때 endsAt에 맞춰 자동 종료/제출하려면 스케줄러가 필요하다.
 * 조회 API에서 매 요청마다 이 UseCase를 호출하는 것은 부작용이 크므로 하지 않는다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutoSubmitParticipantsOnExamEndUseCase {

    private final ExamParticipantRepository examParticipantRepository;
    private final SubmissionService submissionService;
    private final ParticipantSubmitOrchestrationService participantSubmitOrchestrationService;

    @Transactional
    public AutoSubmitResult execute(Long examId) {
        List<ExamParticipant> participants = examParticipantRepository.findAllByExamId(examId);
        int submitted = 0;
        int skippedAlreadySubmitted = 0;
        int skippedNoCodeSnapshot = 0;
        int failed = 0;

        for (ExamParticipant participant : participants) {
            Long participantId = participant.getParticipantId();
            if (submissionService.existsByExamIdAndParticipantId(examId, participantId)) {
                skippedAlreadySubmitted++;
                continue;
            }
            if (!participant.hasCodeSnapshot()) {
                skippedNoCodeSnapshot++;
                log.warn(
                        "Auto-submit skipped (no code snapshot): examId={}, participantId={}, examParticipantId={}",
                        examId, participantId, participant.getId());
                continue;
            }
            try {
                SubmitRequest request = new SubmitRequest(
                        participant.getLastCodeLang(),
                        participant.getLastCodeInline());
                boolean created = participantSubmitOrchestrationService
                        .submitIfAbsent(examId, participantId, request)
                        .isPresent();
                if (created) {
                    submitted++;
                } else {
                    skippedAlreadySubmitted++;
                }
            } catch (RestApiException ex) {
                failed++;
                log.warn("Auto-submit failed for participantId={} examId={}: {}",
                        participantId, examId, ex.getErrorCode().getMessage());
            } catch (Exception ex) {
                failed++;
                log.error("Auto-submit unexpected error for participantId={} examId={}",
                        participantId, examId, ex);
            }
        }

        log.info(
                "Auto-submit on exam end: examId={}, submitted={}, skippedAlready={}, skippedNoCode={}, failed={}",
                examId, submitted, skippedAlreadySubmitted, skippedNoCodeSnapshot, failed);

        return new AutoSubmitResult(submitted, skippedAlreadySubmitted, skippedNoCodeSnapshot, failed);
    }

    public record AutoSubmitResult(
            int submittedCount,
            int skippedAlreadySubmitted,
            int skippedNoCodeSnapshot,
            int failedCount
    ) {}
}
