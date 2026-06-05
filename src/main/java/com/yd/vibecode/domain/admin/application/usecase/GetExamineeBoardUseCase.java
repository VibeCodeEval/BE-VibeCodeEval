package com.yd.vibecode.domain.admin.application.usecase;

import com.yd.vibecode.domain.admin.application.dto.response.ExamineeBoardResponse;
import com.yd.vibecode.domain.admin.application.service.ExamParticipantDisplayStatusResolver;
import com.yd.vibecode.domain.auth.domain.entity.User;
import com.yd.vibecode.domain.exam.domain.entity.Exam;
import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.exam.domain.repository.ExamParticipantRepository;
import com.yd.vibecode.domain.exam.domain.repository.ExamRepository;
import com.yd.vibecode.domain.auth.domain.repository.UserRepository;
import com.yd.vibecode.domain.submission.domain.entity.Score;
import com.yd.vibecode.domain.submission.domain.entity.Submission;
import com.yd.vibecode.domain.submission.domain.repository.ScoreRepository;
import com.yd.vibecode.domain.submission.domain.repository.SubmissionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetExamineeBoardUseCase {

    private final ExamRepository examRepository;
    private final ExamParticipantRepository examParticipantRepository;
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;
    private final ScoreRepository scoreRepository;

    @Transactional(readOnly = true)
    public List<ExamineeBoardResponse> execute(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new com.yd.vibecode.global.exception.RestApiException(
                        com.yd.vibecode.global.exception.code.status.ExamErrorStatus.EXAM_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();

        List<ExamParticipant> participants = examParticipantRepository.findAllByExamId(examId);

        List<Long> participantIds = participants.stream()
            .map(ExamParticipant::getParticipantId)
            .toList();

        Map<Long, User> userMap = userRepository.findAllById(participantIds).stream()
            .collect(Collectors.toMap(User::getId, Function.identity()));

        List<Submission> submissions = submissionRepository.findByExamId(examId);
        Map<Long, Submission> submissionByParticipantId = submissions.stream()
            .collect(Collectors.toMap(Submission::getParticipantId, Function.identity(), (a, b) -> a));

        final Map<Long, Score> scoreBySubmissionId;
        if (submissions.isEmpty()) {
            scoreBySubmissionId = Collections.emptyMap();
        } else {
            List<Long> submissionIds = submissions.stream().map(Submission::getId).toList();
            scoreBySubmissionId = scoreRepository.findBySubmissionIdIn(submissionIds).stream()
                .collect(Collectors.toMap(Score::getSubmissionId, Function.identity(), (a, b) -> a));
        }

        return participants.stream()
            .map(ep -> {
                User p = userMap.get(ep.getParticipantId());
                Submission sub = submissionByParticipantId.get(ep.getParticipantId());
                boolean submitted = sub != null;
                Long submissionId = submitted ? sub.getId() : null;
                String submissionStatus = submitted ? sub.getStatus().name() : null;
                LocalDateTime submittedAt = submitted ? sub.getCreatedAt() : null;
                BigDecimal promptScore = null;
                BigDecimal perfScore = null;
                BigDecimal correctnessScore = null;
                BigDecimal totalScore = null;
                LocalDateTime evaluatedAt = null;
                Score score = null;
                if (submitted) {
                    score = scoreBySubmissionId.get(sub.getId());
                    if (score != null) {
                        promptScore = score.getPromptScore();
                        perfScore = score.getPerfScore();
                        correctnessScore = score.getCorrectnessScore();
                        totalScore = score.getTotalScore();
                        evaluatedAt = score.getUpdatedAt() != null ? score.getUpdatedAt() : score.getCreatedAt();
                    }
                }
                var submissionDisplayStatus = ExamParticipantDisplayStatusResolver.resolveSubmissionDisplayStatus(
                        submitted, sub, score);
                var attendanceStatus = ExamParticipantDisplayStatusResolver.resolveAttendanceStatus(
                        exam, ep, submitted, sub, score, now);

                return ExamineeBoardResponse.of(
                    ep,
                    p,
                    submitted,
                    submissionId,
                    submissionStatus,
                    promptScore,
                    perfScore,
                    correctnessScore,
                    totalScore,
                    submittedAt,
                    evaluatedAt,
                    attendanceStatus,
                    submissionDisplayStatus
                );
            })
            .toList();
    }
}
