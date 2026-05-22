package com.yd.vibecode.domain.submission.ui;

import java.util.Objects;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.yd.vibecode.domain.submission.domain.entity.Submission;
import com.yd.vibecode.domain.submission.domain.service.SubmissionService;
import com.yd.vibecode.domain.submission.infrastructure.SseEmitterRegistry;
import com.yd.vibecode.global.annotation.CurrentUser;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.GlobalErrorStatus;
import com.yd.vibecode.global.swagger.SubmissionStreamApi;

import lombok.RequiredArgsConstructor;

/**
 * 응시자 전용 채점 SSE.
 * <p>
 * {@code GET /api/submissions/{submissionId}/stream} — 인증된 사용자만 접근하며,
 * 제출의 {@code participantId}가 액세스 토큰의 user id와 일치할 때만 연결한다.
 * <p>
 * 이벤트 페이로드는 {@link com.yd.vibecode.domain.submission.infrastructure.SseRetryExecutor}가
 * 관리자 스트림과 동일하게 보내는 범위(case_result, final_score)로 제한된다(코드·루브릭 미포함).
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/submissions")
public class ExamineeSubmissionStreamController implements SubmissionStreamApi {

    private final SubmissionService submissionService;
    private final SseEmitterRegistry sseEmitterRegistry;

    @Override
    @GetMapping(value = "/{submissionId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamScoringResultForExaminee(
            @PathVariable Long submissionId,
            @CurrentUser Long currentUserId) {
        Submission submission = submissionService.findById(submissionId);
        if (!Objects.equals(submission.getParticipantId(), currentUserId)) {
            throw new RestApiException(GlobalErrorStatus._FORBIDDEN);
        }
        return sseEmitterRegistry.register(submissionId);
    }
}
