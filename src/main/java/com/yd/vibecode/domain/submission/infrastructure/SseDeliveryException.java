package com.yd.vibecode.domain.submission.infrastructure;

/**
 * SSE 이벤트 전송 실패 예외
 * SseRetryExecutor가 이 예외를 잡아 재시도를 수행한다.
 */
public class SseDeliveryException extends RuntimeException {

    private final Long submissionId;
    private final String eventName;

    public SseDeliveryException(Long submissionId, String eventName, Throwable cause) {
        super(String.format("SSE delivery failed: submissionId=%d, event=%s", submissionId, eventName), cause);
        this.submissionId = submissionId;
        this.eventName = eventName;
    }

    public Long getSubmissionId() {
        return submissionId;
    }

    public String getEventName() {
        return eventName;
    }
}
