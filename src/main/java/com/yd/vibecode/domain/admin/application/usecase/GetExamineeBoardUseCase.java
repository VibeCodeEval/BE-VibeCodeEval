package com.yd.vibecode.domain.admin.application.usecase;

import com.yd.vibecode.domain.admin.application.dto.response.ExamineeBoardResponse;
import com.yd.vibecode.domain.auth.domain.entity.User;
import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.exam.domain.repository.ExamParticipantRepository;
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
        // ExamParticipantRepository에 findByExamId가 추가되었다고 가정
        List<ExamParticipant> participants = examParticipantRepository.findAllByExamId(examId);
        
        // 참가자 ID 수집
        List<Long> participantIds = participants.stream()
            .map(ExamParticipant::getParticipantId)
            .toList();
            
        // 참가자 상세 정보 조회
        Map<Long, User> userMap = userRepository.findAllById(participantIds).stream()
            .collect(Collectors.toMap(User::getId, p -> p));

        // ExamineeBoardResponse로 매핑
        return participants.stream()
            .map(ep -> {
                User p = userMap.get(ep.getParticipantId());
                // 참고: 제출 상태는 submission 도메인의 SubmissionRepository가 필요함 (아직 미구현)
                // submission 도메인 준비 시 추가: submissionRepository.existsByExamIdAndParticipantId(examId, ep.getParticipantId())
                Boolean submitted = false; 
                return ExamineeBoardResponse.of(ep, p, submitted);
            })
            .toList();
    }
}
