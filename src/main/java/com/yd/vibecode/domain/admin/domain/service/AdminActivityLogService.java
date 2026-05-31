package com.yd.vibecode.domain.admin.domain.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.admin.domain.entity.AdminActivityLog;
import com.yd.vibecode.domain.admin.domain.entity.AdminActivityLogType;
import com.yd.vibecode.domain.admin.domain.repository.AdminActivityLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminActivityLogService {

    private static final String ROOM_CREATED_TITLE = "시험 방 생성됨";
    private static final String ROOM_CREATED_MESSAGE = "새 입장 코드가 생성되었습니다.";
    private static final String EXAM_STARTED_TITLE = "시험 세션이 시작되었습니다.";
    private static final String EXAM_STARTED_MESSAGE = "시험이 시작되었습니다.";
    private static final String EVALUATION_COMPLETED_TITLE = "채점 과정이 성공적으로 완료되었습니다.";
    private static final String EVALUATION_COMPLETED_MESSAGE = "참가자의 채점이 완료되었습니다.";
    private static final String EXAM_ENDED_TITLE = "시험이 종료되었습니다.";
    private static final String EXAM_ENDED_MESSAGE = "시험이 종료되었습니다.";

    private final AdminActivityLogRepository adminActivityLogRepository;

    @Transactional
    public AdminActivityLog logRoomCreated(Long adminId, Long examId) {
        return save(adminId, examId, null, AdminActivityLogType.ROOM_CREATED,
                ROOM_CREATED_TITLE, ROOM_CREATED_MESSAGE);
    }

    @Transactional
    public AdminActivityLog logExamStarted(Long adminId, Long examId) {
        return save(adminId, examId, null, AdminActivityLogType.EXAM_STARTED,
                EXAM_STARTED_TITLE, EXAM_STARTED_MESSAGE);
    }

    @Transactional
    public AdminActivityLog logEvaluationCompleted(Long adminId, Long examId, Long participantId) {
        return save(adminId, examId, participantId, AdminActivityLogType.EVALUATION_COMPLETED,
                EVALUATION_COMPLETED_TITLE, EVALUATION_COMPLETED_MESSAGE);
    }

    @Transactional
    public AdminActivityLog logExamEnded(Long adminId, Long examId) {
        return save(adminId, examId, null, AdminActivityLogType.EXAM_ENDED,
                EXAM_ENDED_TITLE, EXAM_ENDED_MESSAGE);
    }

    private AdminActivityLog save(
            Long adminId,
            Long examId,
            Long participantId,
            AdminActivityLogType type,
            String title,
            String message) {
        AdminActivityLog log = AdminActivityLog.builder()
                .adminId(adminId)
                .examId(examId)
                .participantId(participantId)
                .type(type)
                .title(title)
                .message(message)
                .build();
        return adminActivityLogRepository.save(log);
    }
}
