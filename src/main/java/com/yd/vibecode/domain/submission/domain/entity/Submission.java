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
 * Submission 엔티티
 * - 사용자의 코드 제출
 * - Redis Queue를 통한 비동기 채점
 * - 한 참가자는 한 번만 제출 가능 (exam_id, participant_id unique constraint)
 */
@Entity
@Table(name = "submissions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"exam_id", "participant_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Submission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long examId;

    @Column(nullable = false)
    private Long participantId;

    @Column(nullable = false)
    private Long specId;  // 문제 스펙 ID

    @Column(nullable = false, length = 50)
    private String lang;  // 언어 (예: python3.11, cpp17)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubmissionStatus status;

    @Column(columnDefinition = "TEXT")
    private String codeInline;  // 코드 내용

    @Column(length = 64)
    private String codeSha256;  // 코드 해시

    @Column
    private Integer codeBytes;  // 코드 바이트 수

    @Column
    private Integer codeLoc;  // 코드 라인 수

    @Builder
    public Submission(Long examId, Long participantId, Long specId, String lang,
                     SubmissionStatus status, String codeInline, String codeSha256,
                     Integer codeBytes, Integer codeLoc) {
        this.examId = examId;
        this.participantId = participantId;
        this.specId = specId;
        this.lang = lang;
        this.status = status != null ? status : SubmissionStatus.QUEUED;
        this.codeInline = codeInline;
        this.codeSha256 = codeSha256;
        this.codeBytes = codeBytes;
        this.codeLoc = codeLoc;
    }

    public void updateStatus(SubmissionStatus status) {
        this.status = status;
    }
}
