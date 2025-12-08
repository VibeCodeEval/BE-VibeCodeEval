package com.yd.vibecode.domain.chat.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yd.vibecode.domain.chat.application.dto.request.AISendMessageRequest;
import com.yd.vibecode.domain.chat.application.dto.response.SendMessageResponse;
import com.yd.vibecode.domain.submission.application.dto.request.AISubmitEvaluationRequest;

import lombok.extern.slf4j.Slf4j;

/**
 * AI Chat Service
 * - BE → AI 서버 HTTP 통신 담당 (Sync)
 */
@Slf4j
@Service
public class AIChatService {

    @Value("${ai.server.url}")
    private String aiServerUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public AIChatService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        // RestTemplate 사용 - Uvicorn과의 호환성 문제 해결
        this.restTemplate = new RestTemplate();
    }

    public SendMessageResponse sendMessage(AISendMessageRequest request) {
        log.info("Sending message to AI: session={}, participant={}, turn={}", 
                request.sessionId(), request.participantId(), request.turnId());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AISendMessageRequest> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<SendMessageResponse> response = restTemplate.exchange(
                aiServerUrl + "/api/chat/messages",
                HttpMethod.POST,
                entity,
                SendMessageResponse.class
        );
        
        return response.getBody();
    }

    public void submitEvaluation(AISubmitEvaluationRequest request) {
        try {
            // 요청 본문을 JSON으로 직렬화하여 로그 출력
            String requestJson = objectMapper.writeValueAsString(request);
            log.info("Submitting evaluation to AI: URL={}, Request={}", 
                    aiServerUrl + "/api/session/submit", requestJson);
            log.info("Request details: examId={}, participantId={}, problemId={}, specId={}, submissionId={}, language={}, codeLength={}", 
                    request.examId(), request.participantId(), request.problemId(), 
                    request.specId(), request.submissionId(), request.language(), 
                    request.finalCode() != null ? request.finalCode().length() : 0);
            
            // RestTemplate 사용 - HttpEntity로 명시적으로 Content-Type과 body 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<AISubmitEvaluationRequest> entity = new HttpEntity<>(request, headers);
            
            restTemplate.exchange(
                    aiServerUrl + "/api/session/submit",
                    HttpMethod.POST,
                    entity,
                    Void.class
            );
            
            log.info("AI evaluation request sent successfully: submissionId={}", request.submissionId());
        } catch (RestClientException e) {
            log.error("Failed to send AI evaluation request: submissionId={}, error={}", 
                    request.submissionId(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while sending AI evaluation request: submissionId={}", 
                    request.submissionId(), e);
            throw new RuntimeException("Failed to send evaluation request to AI server", e);
        }
    }
}
