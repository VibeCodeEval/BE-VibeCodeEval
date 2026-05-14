package com.yd.vibecode.domain.admin.application.usecase;

import com.yd.vibecode.domain.admin.application.dto.response.ExamineeBoardResponse;
import com.yd.vibecode.domain.auth.domain.entity.User;
import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.exam.domain.repository.ExamParticipantRepository;
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

    private final ExamParticipantRepository examParticipantRepository;
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;
    private final ScoreRepository scoreRepository;

    @Transactional(readOnly = true)
    public List<ExamineeBoardResponse> execute(Long examId) {
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
                // submissions.participant_id는 원칙적으로 users.id (= exam_participants.participant_id).
                // 레거시/외부 데이터에서 exam_participants.id가 들어간 경우를 보조 매칭한다.
                Submission sub = submissionByParticipantId.get(ep.getParticipantId());
                if (sub == null) {
                    sub = submissionByParticipantId.get(ep.getId());
                }
                boolean submitted = sub != null;
                Long submissionId = submitted ? sub.getId() : null;
                String submissionStatus = submitted ? sub.getStatus().name() : null;
                LocalDateTime submittedAt = submitted ? sub.getCreatedAt() : null;
                BigDecimal totalScore = null;
                LocalDateTime evaluatedAt = null;
                if (submitted) {
                    Score score = scoreBySubmissionId.get(sub.getId());
                    if (score != null) {
                        totalScore = score.getTotalScore();
                        evaluatedAt = score.getUpdatedAt() != null ? score.getUpdatedAt() : score.getCreatedAt();
                    }
                }
                return ExamineeBoardResponse.of(
                    ep,
                    p,
                    submitted,
                    submissionId,
                    submissionStatus,
                    totalScore,
                    submittedAt,
                    evaluatedAt
                );
            })
            .toList();
    }
}
