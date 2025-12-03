package com.yd.vibecode.domain.submission.domain.entity;

import com.yd.vibecode.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * SubmissionRun 엔티티
 * - 제출에 대한 개별 테스트 케이스 실행 결과
 * - (submissionId, caseIndex) 유니크 제약
 */
@Entity
@Table(name = "submission_runs",
       uniqueConstraints = @UniqueConstraint(columnNames = {"submission_id", "case_index"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubmissionRun extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long submissionId;

    @Column(nullable = false)
    private Integer caseIndex;  // 테스트 케이스 인덱스

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RunGroup grp;  // 테스트 그룹

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Verdict verdict;  // 판정 결과

    @Column
    private Integer timeMs;  // 실행 시간 (ms)

    @Column
    private Integer memKb;  // 메모리 사용량 (KB)

    @Column
    private Integer stdoutBytes;  // 표준 출력 바이트 수

    @Column
    private Integer stderrBytes;  // 표준 에러 바이트 수

    @Builder
    public SubmissionRun(Long submissionId, Integer caseIndex, RunGroup grp,
                        Verdict verdict, Integer timeMs, Integer memKb,
                        Integer stdoutBytes, Integer stderrBytes) {
        this.submissionId = submissionId;
        this.caseIndex = caseIndex;
        this.grp = grp;
        this.verdict = verdict;
        this.timeMs = timeMs;
        this.memKb = memKb;
        this.stdoutBytes = stdoutBytes;
        this.stderrBytes = stderrBytes;
    }
}
