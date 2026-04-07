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
        String url = aiServerUrl + "/api/chat/messages";
        log.info("[AIChatService] Sending message to AI: URL={}, sessionId={}, participantId={}, turnId={}", 
                url, request.sessionId(), request.participantId(), request.turnId());
        
        try {
            // FastAPI 요청 형식에 맞게 변환
            // FastAPI는 ChatMessagesRequest를 기대: { sessionId, participantId, turnId, role, content, context }
            java.util.Map<String, Object> fastApiRequest = new java.util.HashMap<>();
            fastApiRequest.put("sessionId", request.sessionId());
            fastApiRequest.put("participantId", request.participantId());
            fastApiRequest.put("turnId", request.turnId());
            fastApiRequest.put("role", request.role());
            fastApiRequest.put("content", request.content());
            fastApiRequest.put("context", request.context());
            
            // 요청 로그 (개발 모드)
            try {
                String requestJson = objectMapper.writeValueAsString(fastApiRequest);
                log.debug("[AIChatService] Request payload: {}", requestJson);
            } catch (Exception e) {
                log.warn("[AIChatService] Failed to serialize request for logging: {}", e.getMessage());
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<java.util.Map<String, Object>> entity = new HttpEntity<>(fastApiRequest, headers);
            
            // AI 서버는 { "aiMessage": { ... } } 형태로 응답하므로, Map으로 받아서 추출
            @SuppressWarnings("unchecked")
            ResponseEntity<java.util.Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    (Class<java.util.Map<String, Object>>) (Class<?>) java.util.Map.class
            );
            
            // HTTP 상태 코드 확인
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("[AIChatService] AI 서버 응답 실패: status={}, body={}", 
                        response.getStatusCode(), response.getBody());
                throw new RuntimeException(
                        String.format("AI 서버 응답 실패: HTTP %s", response.getStatusCode().value()));
            }
            
            // aiMessage 객체 추출
            java.util.Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                log.error("[AIChatService] AI 서버 응답이 null입니다.");
                throw new RuntimeException("AI 서버 응답이 null입니다.");
            }
            
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> aiMessage = (java.util.Map<String, Object>) responseBody.get("aiMessage");
            if (aiMessage == null) {
                log.error("[AIChatService] AI 서버 응답에 aiMessage가 없습니다. responseBody={}", responseBody);
                throw new RuntimeException("AI 서버 응답에 aiMessage가 없습니다.");
            }
            
            // AIMessageInfo를 SendMessageResponse로 변환
            // FastAPI: totalToken, BE: totalCount
            Long sessionId = ((Number) aiMessage.get("sessionId")).longValue();
            Integer turn = ((Number) aiMessage.get("turn")).intValue();
            String role = (String) aiMessage.get("role");
            String content = (String) aiMessage.get("content");
            Integer tokenCount = aiMessage.get("tokenCount") != null ? 
                    ((Number) aiMessage.get("tokenCount")).intValue() : null;
            Integer totalToken = aiMessage.get("totalToken") != null ? 
                    ((Number) aiMessage.get("totalToken")).intValue() : null;
            
            log.info("[AIChatService] AI 응답 수신 성공: sessionId={}, turn={}, role={}, tokenCount={}, totalToken={}", 
                    sessionId, turn, role, tokenCount, totalToken);
            
            return new SendMessageResponse(
                    sessionId,
                    turn,
                    role != null ? role : "AI",
                    content != null ? content : "",
                    tokenCount,
                    totalToken  // totalToken -> totalCount로 매핑
            );
            
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("[AIChatService] AI 서버 HTTP 클라이언트 에러: status={}, responseBody={}", 
                    e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new RuntimeException(
                    String.format("AI 서버 요청 실패: HTTP %s - %s", 
                            e.getStatusCode().value(), e.getResponseBodyAsString()), e);
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            log.error("[AIChatService] AI 서버 HTTP 서버 에러: status={}, responseBody={}", 
                    e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new RuntimeException(
                    String.format("AI 서버 내부 오류: HTTP %s - %s", 
                            e.getStatusCode().value(), e.getResponseBodyAsString()), e);
        } catch (RestClientException e) {
            log.error("[AIChatService] AI 서버 통신 실패: URL={}, error={}", url, e.getMessage(), e);
            throw new RuntimeException(
                    String.format("AI 서버에 연결할 수 없습니다: %s", e.getMessage()), e);
        } catch (Exception e) {
            log.error("[AIChatService] 예상치 못한 오류 발생: URL={}, error={}", url, e.getMessage(), e);
            throw new RuntimeException(
                    String.format("AI 서버 호출 중 오류 발생: %s", e.getMessage()), e);
        }
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
