package com.yd.vibecode.domain.statistics.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "시험 통계 응답")
public record ExamStatisticResponse(
    @Schema(description = "통계 ID", example = "1")
    Long id,
    
    @Schema(description = "시험 ID", example = "1")
    Long examId,
    
    @Schema(description = "버킷 시작 시간", example = "2025-11-30T10:00:00")
    LocalDateTime bucketStart,
    
    @Schema(description = "버킷 시간(초)", example = "60")
    Integer bucketSec,
    
    @Schema(description = "활성 응시자 수", example = "50")
    Integer activeExaminees,
    
    @Schema(description = "채점 큐 깊이", example = "10")
    Integer judgeQueueDepth,
    
    @Schema(description = "평균 대기 시간(초)", example = "5.5")
    BigDecimal avgWaitSec,
    
    @Schema(description = "총 제출 수", example = "200")
    Integer totalSubmissions,
    
    @Schema(description = "통과 제출 수", example = "150")
    Integer passedSubmissions,
    
    @Schema(description = "통과율", example = "75.00")
    BigDecimal passRate,
    
    @Schema(description = "평균 총점", example = "85.50")
    BigDecimal avgTotalScore,
    
    @Schema(description = "평균 프롬프트 점수", example = "40.00")
    BigDecimal avgPromptScore,
    
    @Schema(description = "평균 성능 점수", example = "45.50")
    BigDecimal avgPerfScore,
    
    @Schema(description = "총 토큰 사용량", example = "100000")
    Integer totalTokensUsed,
    
    @Schema(description = "사용자당 평균 토큰", example = "2000.00")
    BigDecimal avgTokensPerUser
) {
}
