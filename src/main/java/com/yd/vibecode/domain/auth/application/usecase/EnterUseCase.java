package com.yd.vibecode.domain.auth.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.auth.application.dto.request.EnterRequest;
import com.yd.vibecode.domain.auth.application.dto.response.EnterResponse;
import com.yd.vibecode.domain.auth.domain.entity.EntryCode;
import com.yd.vibecode.domain.auth.domain.entity.User;
import com.yd.vibecode.domain.auth.domain.service.EntryCodeService;
import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.exam.domain.repository.ExamParticipantRepository;
import com.yd.vibecode.domain.exam.domain.service.ExamParticipantService;
import com.yd.vibecode.domain.auth.domain.service.UserService;
import com.yd.vibecode.global.security.TokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EnterUseCase {

    private final EntryCodeService entryCodeService;
    private final UserService userService;
    private final ExamParticipantService examParticipantService;
    private final ExamParticipantRepository examParticipantRepository;
    private final TokenProvider tokenProvider;
    private final com.yd.vibecode.domain.exam.domain.service.ExamService examService;

    @Transactional
    public EnterResponse execute(EnterRequest request) {
        // 1. 입장코드 검증
        EntryCode entryCode = entryCodeService.findByCode(request.code());
        entryCodeService.validateEntryCode(entryCode);

        // 2. 참가자 찾기 또는 생성
        User user = userService.findByPhone(request.phone());
        if (user == null) {
            user = userService.create(request.name(), request.phone());
        } else {
            // 기존 참가자 이름 업데이트 (필요시)
            if (!user.getName().equals(request.name())) {
                user.updateName(request.name());
            }
        }

        // 3. 시험 참가자 세션 찾기 또는 생성
        ExamParticipant examParticipant = examParticipantService.findByExamIdAndParticipantId(
                entryCode.getExamId(), user.getId());

        if (examParticipant == null) {
            examParticipant = examParticipantService.create(
                    entryCode.getExamId(),
                    user.getId(),
                    entryCode.getProblemSetId(),
                    entryCode.getMaxUses() > 0 ? entryCode.getMaxUses() * 1000 : 20000 // 기본 토큰 한도
            );
        }

        // 4. JWT 생성
        String accessToken = tokenProvider.createAccessToken(
                user.getId().toString(), "USER");

        // 5. 입장코드 사용 횟수 증가 및 flush
        entryCodeService.incrementUsedCount(entryCode);
        examParticipantRepository.flush(); // 트랜잭션 커밋 전 flush

        // 6. Exam 정보 조회
        com.yd.vibecode.domain.exam.domain.entity.Exam exam = examService.findById(entryCode.getExamId());

        // 7. ResponseDTO 구성
        return new EnterResponse(
                accessToken,
                "USER",
                new EnterResponse.ParticipantInfo(
                        user.getId(),
                        user.getName(),
                        user.getPhone()
                ),
                new com.yd.vibecode.domain.exam.application.dto.response.ExamInfoResponse(
                        exam.getId(),
                        exam.getTitle(),
                        exam.getState().name()
                ),
                new com.yd.vibecode.domain.exam.application.dto.response.SessionInfoResponse(
                        examParticipant.getId(),
                        examParticipant.getTokenLimit(),
                        examParticipant.getTokenUsed()
                )
        );
    }
}

