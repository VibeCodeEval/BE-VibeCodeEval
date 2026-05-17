package com.yd.vibecode.domain.submission.application.dto.response;

import java.util.List;

import com.yd.vibecode.domain.submission.domain.entity.RunGroup;
import com.yd.vibecode.domain.submission.domain.entity.SubmissionStatus;
import com.yd.vibecode.domain.submission.domain.entity.Verdict;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 관리자 전용 제출 상세 (제출 코드·루브릭 JSON 포함).
 * 일반 {@link SubmissionDetailResponse}에는 민감 필드를 넣지 않는다.
 */
@Schema(description = "관리자 전용 제출 상세 응답")
public record AdminSubmissionDetailResponse(
        @Schema(description = "제출 ID", example = "1")
        Long submissionId,

        @Schema(description = "제출 상태")
        SubmissionStatus status,

        @Schema(description = "언어/런타임", example = "python3.11")
        String lang,

        @Schema(description = "제출 소스 코드 본문 (민감)")
        String codeInline,

        @Schema(description = "실행 메트릭")
        SubmissionDetailResponse.MetricsInfo metrics,

        @Schema(description = "테스트 케이스 그룹별 집계")
        SubmissionDetailResponse.TestCaseInfo tc,

        @Schema(description = "채점 점수")
        SubmissionDetailResponse.ScoreInfo score,

        @Schema(description = "AI/채점 루브릭 JSON (민감, scores.rubric_json)")
        String rubricJson,

        @Schema(description = "개별 테스트 케이스 실행 결과 (submission_runs, 관리자 전용)")
        List<CaseRunInfo> runs
) {

    @Schema(description = "단일 테스트 케이스 실행 결과")
    public record CaseRunInfo(
            @Schema(description = "케이스 인덱스", example = "0")
            Integer caseIndex,

            @Schema(description = "테스트 그룹")
            RunGroup grp,

            @Schema(description = "판정")
            Verdict verdict,

            @Schema(description = "실행 시간(ms)", example = "120")
            Integer timeMs,

            @Schema(description = "메모리(KB)", example = "2048")
            Integer memKb
    ) {
    }
}
