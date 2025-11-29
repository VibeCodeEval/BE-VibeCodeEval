package com.yd.vibecode.domain.submission.ui;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.yd.vibecode.global.sse.SseEmitterService;

import lombok.RequiredArgsConstructor;

/**
 * SSE Controller (Admin/Master용)
 * - GET /api/admin/submissions/{submissionId}/stream: 실시간 채점 결과 스트리밍
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/submissions")
public class SseSubmissionController {

    private final SseEmitterService sseEmitterService;

    @GetMapping(value = "/{submissionId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamScoringResult(@PathVariable Long submissionId) {
        return sseEmitterService.createEmitter(submissionId);
    }
}
