package com.yd.vibecode.domain.exam.application.usecase;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.auth.domain.entity.EntryCode;
import com.yd.vibecode.domain.auth.domain.repository.EntryCodeRepository;
import com.yd.vibecode.domain.exam.application.dto.event.ExamStateEvent;
import com.yd.vibecode.domain.exam.domain.entity.Exam;
import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.exam.domain.repository.ExamParticipantRepository;
import com.yd.vibecode.domain.exam.domain.service.ExamService;
import com.yd.vibecode.domain.problem.domain.entity.Problem;
import com.yd.vibecode.domain.problem.domain.repository.ProblemRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 시험 시작 UseCase
 * - 시험 상태를 WAITING -> RUNNING으로 변경
 * - version 증가
 * - 대기 중인 모든 참가자의 specId를 문제의 최신 currentSpecId로 동기화
 * - WebSocket으로 상태 변경 브로드캐스트 (/topic/exam/{examId})
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StartExamUseCase {

    private final ExamService examService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ExamParticipantRepository examParticipantRepository;
    private final ProblemRepository problemRepository;
    private final EntryCodeRepository entryCodeRepository;

    @Transactional
    public void execute(Long examId) {
        // 1. 참가자 specId 및 tokenLimit 동기화 (시험 상태 변경 전 처리)
        syncParticipants(examId);

        // 2. 시험 상태 변경 WAITING -> RUNNING
        Exam exam = examService.startExam(examId);

        // 3. WebSocket 브로드캐스트
        ExamStateEvent event = ExamStateEvent.from(exam);
        messagingTemplate.convertAndSend("/topic/exam/" + examId, event);
        log.info("Exam started, WS broadcast sent: examId={}, version={}", examId, exam.getVersion());
    }

    /**
     * 시험에 속한 모든 참가자의 specId와 tokenLimit을 최신 값으로 동기화한다.
     * - specId: 참가자의 assignedProblemId 기준으로 문제의 currentSpecId를 적용
     * - tokenLimit: 해당 시험의 활성 입장 코드 tokenLimit을 적용
     * - 문제 조회는 IN 쿼리 1회로 처리해 N+1을 방지한다.
     * - 복수의 활성 입장 코드가 있을 경우 첫 번째(최신)를 기준으로 한다.
     */
    private void syncParticipants(Long examId) {
        List<ExamParticipant> participants = examParticipantRepository.findByExamId(examId);
        if (participants.isEmpty()) {
            return;
        }

        // 활성 입장 코드에서 tokenLimit 조회
        List<EntryCode> activeCodes = entryCodeRepository.findByExamIdAndIsActive(examId, true);
        Integer latestTokenLimit = activeCodes.isEmpty() ? null : activeCodes.get(0).getTokenLimit();

        // 참가자가 배정받은 문제 ID를 수집해 IN 쿼리 1회로 일괄 조회 (N+1 방지)
        Set<Long> problemIds = participants.stream()
                .map(ExamParticipant::getAssignedProblemId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, Long> problemIdToSpecId = problemRepository.findAllById(problemIds).stream()
                .filter(p -> p.getCurrentSpecId() != null)
                .collect(Collectors.toMap(Problem::getId, Problem::getCurrentSpecId));

        int syncedSpecId = 0;
        int syncedTokenLimit = 0;

        for (ExamParticipant participant : participants) {
            // specId 동기화
            Long assignedProblemId = participant.getAssignedProblemId();
            if (assignedProblemId != null) {
                Long currentSpecId = problemIdToSpecId.get(assignedProblemId);
                if (currentSpecId != null && !currentSpecId.equals(participant.getSpecId())) {
                    participant.updateSpecId(currentSpecId);
                    syncedSpecId++;
                }
            }

            // tokenLimit 동기화 (입장 코드의 최신 tokenLimit으로)
            if (latestTokenLimit != null && !latestTokenLimit.equals(participant.getTokenLimit())) {
                participant.updateTokenLimit(latestTokenLimit);
                syncedTokenLimit++;
            }
        }

        log.info("Participant sync completed: examId={}, totalParticipants={}, specIdSynced={}, tokenLimitSynced={}",
                examId, participants.size(), syncedSpecId, syncedTokenLimit);
    }
}
