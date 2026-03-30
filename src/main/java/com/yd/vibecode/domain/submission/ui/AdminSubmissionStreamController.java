package com.yd.vibecode.domain.submission.ui;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.yd.vibecode.domain.submission.infrastructure.SseEmitterRegistry;
import com.yd.vibecode.global.swagger.AdminSubmissionStreamApi;

import lombok.RequiredArgsConstructor;

/**
 * 채점 결과 SSE 스트리밍 컨트롤러 (Admin/Master 전용)
 *
 * GET /api/admin/submissions/{submissionId}/stream
 * - text/event-stream 응답
 * - 이벤트: case_result (테스트 케이스별), final_score (최종 점수)
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/submissions")
public class AdminSubmissionStreamController implements AdminSubmissionStreamApi {

    private final SseEmitterRegistry sseEmitterRegistry;

    @GetMapping(value = "/{submissionId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamScoringResult(@PathVariable Long submissionId) {
        return sseEmitterRegistry.register(submissionId);
    }
}
