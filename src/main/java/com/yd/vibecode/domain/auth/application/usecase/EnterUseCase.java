package com.yd.vibecode.domain.auth.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.auth.application.dto.request.EnterRequest;
import com.yd.vibecode.domain.auth.application.dto.response.EnterResponse;
import com.yd.vibecode.domain.auth.domain.entity.EntryCode;
import com.yd.vibecode.domain.auth.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.auth.domain.entity.Participant;
import com.yd.vibecode.domain.auth.domain.repository.ExamParticipantRepository;
import com.yd.vibecode.domain.auth.domain.service.EntryCodeService;
import com.yd.vibecode.domain.auth.domain.service.ExamParticipantService;
import com.yd.vibecode.domain.auth.domain.service.ParticipantService;
import com.yd.vibecode.global.security.TokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EnterUseCase {

    private final EntryCodeService entryCodeService;
    private final ParticipantService participantService;
    private final ExamParticipantService examParticipantService;
    private final ExamParticipantRepository examParticipantRepository;
    private final TokenProvider tokenProvider;

    @Transactional
    public EnterResponse execute(EnterRequest request) {
        // 1. 입장코드 검증
        EntryCode entryCode = entryCodeService.findByCode(request.code());
        entryCodeService.validateEntryCode(entryCode);

        // 2. 참가자 찾기 또는 생성
        Participant participant = participantService.findByPhone(request.phone());
        if (participant == null) {
            participant = participantService.create(request.name(), request.phone());
        } else {
            // 기존 참가자 이름 업데이트 (필요시)
            if (!participant.getName().equals(request.name())) {
                participant.updateName(request.name());
            }
        }

        // 3. 시험 참가자 세션 찾기 또는 생성
        ExamParticipant examParticipant = examParticipantService.findByExamIdAndParticipantId(
                entryCode.getExamId(), participant.getId());

        if (examParticipant == null) {
            examParticipant = examParticipantService.create(
                    entryCode.getExamId(),
                    participant.getId(),
                    null, // specId는 문제 배정 시 설정
                    entryCode.getMaxUses() > 0 ? entryCode.getMaxUses() * 1000 : 20000 // 기본 토큰 한도
            );
        }

        // 4. 입장코드 사용 횟수 증가
        entryCodeService.incrementUsedCount(entryCode);
        examParticipantRepository.flush(); // 트랜잭션 커밋 전 flush

        // 5. JWT 토큰 생성
        String accessToken = tokenProvider.createAccessToken(
                participant.getId().toString(), "USER");

        // 6. 응답 생성
        return new EnterResponse(
                accessToken,
                "USER",
                new EnterResponse.ParticipantInfo(
                        participant.getId(),
                        participant.getName(),
                        participant.getPhone()
                ),
                new EnterResponse.ExamInfo(
                        entryCode.getExamId(),
                        "", // TODO: exam 도메인과 연계하여 exam title 조회 필요
                        "WAITING" // TODO: exam 도메인과 연계하여 exam state 조회 필요
                ),
                new EnterResponse.SessionInfo(
                        examParticipant.getId(),
                        examParticipant.getTokenLimit(),
                        examParticipant.getTokenUsed()
                )
        );
    }
}

