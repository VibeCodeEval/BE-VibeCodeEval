package com.yd.vibecode.domain.chat.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.yd.vibecode.domain.chat.application.dto.request.AISendMessageRequest;
import com.yd.vibecode.domain.chat.application.dto.response.SendMessageResponse;
import com.yd.vibecode.domain.submission.application.dto.request.AISubmitEvaluationRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AI Chat Service
 * - BE → AI 서버 HTTP 통신 담당 (Sync)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIChatService {

    @Value("${ai.server.url}")
    private String aiServerUrl;

    private final RestClient restClient = RestClient.create();

    public SendMessageResponse sendMessage(AISendMessageRequest request) {
        log.info("Sending message to AI: session={}, participant={}, turn={}", 
                request.sessionId(), request.participantId(), request.turnId());
        
        return restClient.post()
                .uri(aiServerUrl + "/api/chat/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(SendMessageResponse.class);
    }

    public void submitEvaluation(AISubmitEvaluationRequest request) {
        log.info("Submitting evaluation to AI: participant={}, submissionId={}, problemId={}", 
                request.participantId(), request.submissionId(), request.problemId());
        
        restClient.post()
                .uri(aiServerUrl + "/api/session/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
