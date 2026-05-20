package com.yd.vibecode.domain.admin.application.dto.response;

import com.yd.vibecode.domain.problem.domain.entity.Difficulty;
import com.yd.vibecode.domain.problem.domain.entity.ProblemStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "관리자/마스터 문제 상세 (공개 스펙만, 테스트케이스 미포함)")
public record ProblemDetailResponse(
    @Schema(description = "문제 ID", example = "1")
    Long id,

    @Schema(description = "문제 제목", example = "외판원 순회")
    String title,

    @Schema(description = "난이도", example = "MEDIUM")
    Difficulty difficulty,

    @Schema(description = "태그 목록", example = "[\"dp\", \"graph\"]")
    List<String> tags,

    @Schema(description = "스펙 버전", example = "1")
    Integer version,

    @Schema(description = "문제 본문 (Markdown)")
    String contentMd,

    @Schema(description = "실행 제한")
    LimitsInfo limits,

    @Schema(description = "언어/API 제한")
    RestrictionsInfo restrictions,

    @Schema(description = "채점기 유형")
    CheckerInfo checker,

    @Schema(description = "문제 생성일시")
    LocalDateTime createdAt,

    @Schema(description = "문제 수정일시")
    LocalDateTime updatedAt,

    @Schema(description = "스펙 게시일시")
    LocalDateTime publishedAt,

    @Schema(description = "문제 상태", example = "PUBLISHED")
    ProblemStatus status,

    @Schema(description = "사용 가능 여부 (PUBLISHED)")
    boolean usable
) {
    public record LimitsInfo(
        @Schema(description = "시간 제한(ms)", example = "2000")
        Integer timeMs,

        @Schema(description = "메모리 제한(MB)", example = "256")
        Integer memoryMb
    ) {
    }

    public record RestrictionsInfo(
        @Schema(description = "허용 언어")
        List<String> allowedLangs,

        @Schema(description = "금지 API")
        List<String> forbiddenApis
    ) {
    }

    public record CheckerInfo(
        @Schema(description = "채점기 타입", example = "equality")
        String type
    ) {
    }
}
