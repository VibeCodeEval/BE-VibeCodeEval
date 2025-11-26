package com.yd.vibecode.domain.admin.application.usecase;

import com.yd.vibecode.domain.admin.application.dto.response.ExamineeBoardResponse;
import com.yd.vibecode.domain.auth.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.auth.domain.entity.User;
import com.yd.vibecode.domain.auth.domain.repository.ExamParticipantRepository;
import com.yd.vibecode.domain.auth.domain.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetExamineeBoardUseCase {

    private final ExamParticipantRepository examParticipantRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ExamineeBoardResponse> execute(Long examId) {
        // Assuming we add findByExamId to ExamParticipantRepository
        List<ExamParticipant> participants = examParticipantRepository.findAllByExamId(examId);
        
        // Collect participant IDs
        List<Long> participantIds = participants.stream()
            .map(ExamParticipant::getParticipantId)
            .toList();
            
        // Fetch Participant details
        Map<Long, User> userMap = userRepository.findAllById(participantIds).stream()
            .collect(Collectors.toMap(User::getId, p -> p));

        // Map to ExamineeBoardResponse
        return participants.stream()
            .map(ep -> {
                User p = userMap.get(ep.getParticipantId());
                // Note: Submission status requires SubmissionRepository from submission domain (not yet implemented)
                // Once submission domain is ready, add: submissionRepository.existsByExamIdAndParticipantId(examId, ep.getParticipantId())
                Boolean submitted = false; 
                return ExamineeBoardResponse.of(ep, p, submitted);
            })
            .toList();
    }
}
