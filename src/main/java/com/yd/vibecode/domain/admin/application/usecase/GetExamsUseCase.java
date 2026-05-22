package com.yd.vibecode.domain.admin.application.usecase;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.repository.AdminRepository;
import com.yd.vibecode.domain.exam.application.dto.response.ExamResponse;
import com.yd.vibecode.domain.exam.domain.entity.Exam;
import com.yd.vibecode.domain.exam.domain.repository.ExamParticipantRepository;
import com.yd.vibecode.domain.exam.domain.repository.ExamRepository;
import com.yd.vibecode.domain.submission.domain.repository.SubmissionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetExamsUseCase {

    private final ExamRepository examRepository;
    private final ExamParticipantRepository examParticipantRepository;
    private final SubmissionRepository submissionRepository;
    private final AdminRepository adminRepository;

    public List<ExamResponse> execute() {
        List<Exam> exams = examRepository.findAll();
        if (exams.isEmpty()) {
            return List.of();
        }

        List<Long> examIds = exams.stream().map(Exam::getId).collect(Collectors.toList());

        // Bulk GROUP BY queries: 2 queries total instead of 2N
        Map<Long, Long> participantCounts = examParticipantRepository.countGroupByExamIdIn(examIds)
                .stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));

        Map<Long, Long> completedCounts = submissionRepository.countGroupByExamIdIn(examIds)
                .stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));

        Set<Long> creatorAdminIds = exams.stream()
                .map(Exam::getCreatedBy)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> creatorNameByAdminId = creatorAdminIds.isEmpty()
                ? Map.of()
                : adminRepository.findByIdIn(creatorAdminIds).stream()
                        .collect(Collectors.toMap(Admin::getId, Admin::resolveDisplayName));

        return exams.stream()
                .map(exam -> ExamResponse.from(
                        exam,
                        participantCounts.getOrDefault(exam.getId(), 0L),
                        completedCounts.getOrDefault(exam.getId(), 0L),
                        resolveCreatorName(creatorNameByAdminId, exam.getCreatedBy())
                ))
                .collect(Collectors.toList());
    }

    private static String resolveCreatorName(Map<Long, String> creatorNameByAdminId, Long createdBy) {
        if (createdBy == null) {
            return null;
        }
        return creatorNameByAdminId.get(createdBy);
    }
}
