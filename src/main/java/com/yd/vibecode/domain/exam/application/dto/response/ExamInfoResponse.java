package com.yd.vibecode.domain.exam.application.dto.response;

/**
 * Exam 정보 응답 DTO
 * - Auth 도메인의 EnterResponse, MeResponse에서 사용
 */
public record ExamInfoResponse(
    Long id,
    String title,
    String state
) {
}
