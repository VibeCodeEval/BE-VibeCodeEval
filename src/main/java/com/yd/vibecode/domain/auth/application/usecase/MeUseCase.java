package com.yd.vibecode.domain.auth.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.auth.application.dto.response.MeResponse;
import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.entity.User;
import com.yd.vibecode.domain.auth.domain.service.AdminService;
import com.yd.vibecode.domain.auth.domain.service.UserService;
import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.exam.domain.service.ExamParticipantService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;
import com.yd.vibecode.global.security.TokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MeUseCase {

    private final TokenProvider tokenProvider;
    private final UserService userService;
    private final AdminService adminService;
    private final ExamParticipantService examParticipantService;
    private final com.yd.vibecode.domain.exam.domain.service.ExamService examService;

    @Transactional(readOnly = true)
    public MeResponse execute(String token) {
        // 1. 토큰에서 사용자 ID와 역할 추출
        String userId = tokenProvider.getId(token)
                .orElseThrow(() -> new RestApiException(AuthErrorStatus.INVALID_ACCESS_TOKEN));
        String role = tokenProvider.getRole(token)
                .orElse("USER");

        Long id;
        try {
            id = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            throw new RestApiException(AuthErrorStatus.INVALID_ACCESS_TOKEN);
        }

        // 2. 역할에 따라 다른 서비스로 정보 조회
        if ("ADMIN".equals(role)) {
            // 관리자 정보 조회
            Admin admin = adminService.findById(id);
            
            return new MeResponse(
                    role,
                    new MeResponse.ParticipantInfo(
                            admin.getId(),
                            admin.getAdminNumber(),  // name 필드에 adminNumber 사용
                            admin.getEmail()         // phone 필드에 email 사용
                    ),
                    null,
                    null
            );
        }

        // 3. 일반 사용자(USER) 정보 조회
        User user = userService.findById(id);

        // 4. 시험 참가자 세션 조회 (가장 최근 것)
        ExamParticipant examParticipant = examParticipantService.findLatestByParticipantId(id);

        // 5. 응답 생성
        if (examParticipant != null) {
            // Exam 정보 조회
            com.yd.vibecode.domain.exam.domain.entity.Exam exam = examService.findById(examParticipant.getExamId());

            // 4. 응답 구성
            return new MeResponse(
                    "USER",
                    new MeResponse.ParticipantInfo(
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

        return new MeResponse(
                role,
                new MeResponse.ParticipantInfo(
                        user.getId(),
                        user.getName(),
                        user.getPhone()
                ),
                null,
                null
        );
    }
}

