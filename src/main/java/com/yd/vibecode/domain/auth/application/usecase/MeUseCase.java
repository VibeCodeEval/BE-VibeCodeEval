package com.yd.vibecode.domain.auth.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.auth.application.dto.response.MeResponse;
import com.yd.vibecode.domain.auth.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.auth.domain.entity.Participant;
import com.yd.vibecode.domain.auth.domain.service.ExamParticipantService;
import com.yd.vibecode.domain.auth.domain.service.ParticipantService;
import com.yd.vibecode.global.security.TokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MeUseCase {

    private final TokenProvider tokenProvider;
    private final ParticipantService participantService;
    private final ExamParticipantService examParticipantService;

    @Transactional(readOnly = true)
    public MeResponse execute(String token) {
        // 1. 토큰에서 사용자 ID와 역할 추출
        String userId = tokenProvider.getId(token)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 토큰입니다."));
        String role = tokenProvider.getRole(token)
                .orElse("USER");

        Long participantId = Long.parseLong(userId);

        // 2. 참가자 정보 조회
        Participant participant = participantService.findById(participantId);

        // 3. 시험 참가자 세션 조회 (가장 최근 것)
        ExamParticipant examParticipant = examParticipantService.findLatestByParticipantId(participantId);

        // 4. 응답 생성
        if (examParticipant != null) {
            // TODO: exam 도메인과 연계하여 exam title 조회 필요
            return new MeResponse(
                    role,
                    new MeResponse.ParticipantInfo(
                            participant.getId(),
                            participant.getName(),
                            participant.getPhone()
                    ),
                    new MeResponse.ExamInfo(
                            examParticipant.getExamId(),
                            "", // TODO: exam 도메인에서 조회 필요
                            examParticipant.getState()
                    ),
                    new MeResponse.SessionInfo(
                            examParticipant.getId(),
                            examParticipant.getTokenLimit(),
                            examParticipant.getTokenUsed(),
                            examParticipant.getAssignedSpecVersion(),
                            examParticipant.getAssignedProblemId()
                    )
            );
        }

        return new MeResponse(
                role,
                new MeResponse.ParticipantInfo(
                        participant.getId(),
                        participant.getName(),
                        participant.getPhone()
                ),
                null,
                null
        );
    }
}

