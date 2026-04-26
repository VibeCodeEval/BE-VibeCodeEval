package com.yd.vibecode.domain.exam.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.yd.vibecode.domain.auth.domain.entity.EntryCode;
import com.yd.vibecode.domain.auth.domain.repository.EntryCodeRepository;
import com.yd.vibecode.domain.exam.application.dto.event.ExamStateEvent;
import com.yd.vibecode.domain.exam.domain.entity.Exam;
import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.exam.domain.entity.ExamState;
import com.yd.vibecode.domain.exam.domain.repository.ExamParticipantRepository;
import com.yd.vibecode.domain.exam.domain.service.ExamService;
import com.yd.vibecode.domain.problem.domain.entity.Difficulty;
import com.yd.vibecode.domain.problem.domain.entity.Problem;
import com.yd.vibecode.domain.problem.domain.entity.ProblemStatus;
import com.yd.vibecode.domain.problem.domain.repository.ProblemRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * StartExamUseCase 버그픽스 검증 테스트
 *
 * <p>버그: 시험 시작 시 대기 중이던 참가자에게 구버전 specId(null)와 tokenLimit(20000)으로
 *         시험이 시작되던 문제
 * <p>수정: execute() 내부에서 먼저 syncParticipants() 를 호출하여
 *         모든 참가자의 specId 와 tokenLimit 을 최신 값으로 갱신한 뒤 상태를 RUNNING 으로 전환
 */
@ExtendWith(MockitoExtension.class)
class StartExamSyncTest {

    @InjectMocks
    private StartExamUseCase startExamUseCase;

    @Mock
    private ExamService examService;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private ExamParticipantRepository examParticipantRepository;
    @Mock
    private ProblemRepository problemRepository;
    @Mock
    private EntryCodeRepository entryCodeRepository;

    // -------------------------------------------------------------------------
    // 1. specId 동기화 — 참가자의 specId 가 문제의 currentSpecId 로 갱신되는지 확인
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("시험 시작 시 참가자의 specId가 문제의 currentSpecId로 동기화된다")
    void execute_syncs_specId_to_current_spec_id_of_problem() {
        // given
        Long examId = 1L;
        Long problemId = 10L;
        Long outdatedSpecId = 100L;
        Long latestSpecId = 200L;

        ExamParticipant participant = ExamParticipant.builder()
                .examId(examId)
                .participantId(1L)
                .assignedProblemId(problemId)
                .specId(outdatedSpecId)  // 구버전 specId
                .tokenLimit(20000)
                .build();
        ReflectionTestUtils.setField(participant, "id", 1L);

        Problem problem = Problem.builder()
                .title("문제 A")
                .difficulty(Difficulty.MEDIUM)
                .status(ProblemStatus.PUBLISHED)
                .currentSpecId(latestSpecId)
                .build();
        ReflectionTestUtils.setField(problem, "id", problemId);

        Exam exam = Exam.builder()
                .title("테스트 시험")
                .state(ExamState.RUNNING)
                .startsAt(LocalDateTime.now())
                .endsAt(LocalDateTime.now().plusHours(2))
                .version(1)
                .createdBy(1L)
                .build();

        given(examParticipantRepository.findByExamId(examId)).willReturn(List.of(participant));
        given(entryCodeRepository.findTopByExamIdAndIsActiveOrderByCreatedAtDesc(examId, true)).willReturn(Optional.empty());
        given(problemRepository.findAllById(Set.of(problemId))).willReturn(List.of(problem));
        given(examService.startExam(examId)).willReturn(exam);

        // when
        startExamUseCase.execute(examId);

        // then — participant.specId 가 최신 값으로 업데이트됐어야 함
        assertThat(participant.getSpecId()).isEqualTo(latestSpecId);
        verify(examService).startExam(examId);
        verify(messagingTemplate).convertAndSend(eq("/topic/exam/" + examId), any(ExamStateEvent.class));
    }

    @Test
    @DisplayName("시험 시작 시 참가자의 specId가 이미 최신이면 updateSpecId가 호출되지 않는다")
    void execute_does_not_update_specId_when_already_current() {
        // given
        Long examId = 2L;
        Long problemId = 11L;
        Long currentSpecId = 201L;

        ExamParticipant participant = ExamParticipant.builder()
                .examId(examId)
                .participantId(2L)
                .assignedProblemId(problemId)
                .specId(currentSpecId)  // 이미 최신 specId
                .tokenLimit(20000)
                .build();
        ReflectionTestUtils.setField(participant, "id", 2L);

        Problem problem = Problem.builder()
                .title("문제 B")
                .difficulty(Difficulty.EASY)
                .status(ProblemStatus.PUBLISHED)
                .currentSpecId(currentSpecId)
                .build();
        ReflectionTestUtils.setField(problem, "id", problemId);

        Exam exam = Exam.builder()
                .title("시험 2")
                .state(ExamState.RUNNING)
                .startsAt(LocalDateTime.now())
                .endsAt(LocalDateTime.now().plusHours(1))
                .version(1)
                .createdBy(1L)
                .build();

        given(examParticipantRepository.findByExamId(examId)).willReturn(List.of(participant));
        given(entryCodeRepository.findTopByExamIdAndIsActiveOrderByCreatedAtDesc(examId, true)).willReturn(Optional.empty());
        given(problemRepository.findAllById(Set.of(problemId))).willReturn(List.of(problem));
        given(examService.startExam(examId)).willReturn(exam);

        // when
        startExamUseCase.execute(examId);

        // then — specId 값이 변경되지 않아야 함
        assertThat(participant.getSpecId()).isEqualTo(currentSpecId);
    }

    @Test
    @DisplayName("시험 시작 시 참가자에게 assignedProblemId가 없으면 specId 동기화를 건너뛴다")
    void execute_skips_specId_sync_when_no_assigned_problem() {
        // given
        Long examId = 3L;

        // assignedProblemId = null 인 참가자
        ExamParticipant participant = ExamParticipant.builder()
                .examId(examId)
                .participantId(3L)
                .assignedProblemId(null)
                .specId(null)
                .tokenLimit(20000)
                .build();
        ReflectionTestUtils.setField(participant, "id", 3L);

        Exam exam = Exam.builder()
                .title("시험 3")
                .state(ExamState.RUNNING)
                .startsAt(LocalDateTime.now())
                .endsAt(LocalDateTime.now().plusHours(1))
                .version(1)
                .createdBy(1L)
                .build();

        given(examParticipantRepository.findByExamId(examId)).willReturn(List.of(participant));
        given(entryCodeRepository.findTopByExamIdAndIsActiveOrderByCreatedAtDesc(examId, true)).willReturn(Optional.empty());
        given(examService.startExam(examId)).willReturn(exam);

        // when
        startExamUseCase.execute(examId);

        // then — problemRepository 조회가 전혀 발생하지 않아야 함
        verifyNoInteractions(problemRepository);
        assertThat(participant.getSpecId()).isNull();
    }

    // -------------------------------------------------------------------------
    // 2. tokenLimit 동기화 — 활성 EntryCode 의 tokenLimit 이 참가자에게 적용되는지 확인
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("시험 시작 시 활성 EntryCode의 tokenLimit으로 참가자 tokenLimit이 동기화된다")
    void execute_syncs_tokenLimit_from_active_entry_code() {
        // given
        Long examId = 4L;
        final int updatedTokenLimit = 50000;

        ExamParticipant participant = ExamParticipant.builder()
                .examId(examId)
                .participantId(4L)
                .assignedProblemId(null)
                .tokenLimit(20000)  // 구버전 기본값
                .build();
        ReflectionTestUtils.setField(participant, "id", 4L);

        EntryCode activeCode = EntryCode.builder()
                .code("CODE-ACTIVE")
                .examId(examId)
                .tokenLimit(updatedTokenLimit)
                .maxUses(0)
                .isActive(true)
                .build();

        Exam exam = Exam.builder()
                .title("시험 4")
                .state(ExamState.RUNNING)
                .startsAt(LocalDateTime.now())
                .endsAt(LocalDateTime.now().plusHours(2))
                .version(1)
                .createdBy(1L)
                .build();

        given(examParticipantRepository.findByExamId(examId)).willReturn(List.of(participant));
        given(entryCodeRepository.findTopByExamIdAndIsActiveOrderByCreatedAtDesc(examId, true)).willReturn(Optional.of(activeCode));
        given(examService.startExam(examId)).willReturn(exam);

        // when
        startExamUseCase.execute(examId);

        // then — 참가자의 tokenLimit 이 EntryCode 값으로 갱신됐어야 함
        assertThat(participant.getTokenLimit()).isEqualTo(updatedTokenLimit);
    }

    @Test
    @DisplayName("시험 시작 시 활성 EntryCode가 없으면 참가자 tokenLimit을 변경하지 않는다")
    void execute_does_not_change_tokenLimit_when_no_active_entry_code() {
        // given
        Long examId = 5L;
        final int originalTokenLimit = 30000;

        ExamParticipant participant = ExamParticipant.builder()
                .examId(examId)
                .participantId(5L)
                .tokenLimit(originalTokenLimit)
                .build();
        ReflectionTestUtils.setField(participant, "id", 5L);

        Exam exam = Exam.builder()
                .title("시험 5")
                .state(ExamState.RUNNING)
                .startsAt(LocalDateTime.now())
                .endsAt(LocalDateTime.now().plusHours(1))
                .version(1)
                .createdBy(1L)
                .build();

        given(examParticipantRepository.findByExamId(examId)).willReturn(List.of(participant));
        given(entryCodeRepository.findTopByExamIdAndIsActiveOrderByCreatedAtDesc(examId, true)).willReturn(Optional.empty());
        given(examService.startExam(examId)).willReturn(exam);

        // when
        startExamUseCase.execute(examId);

        // then — tokenLimit 이 원래 값 그대로 유지돼야 함
        assertThat(participant.getTokenLimit()).isEqualTo(originalTokenLimit);
    }

    @Test
    @DisplayName("시험 시작 시 참가자 tokenLimit이 이미 EntryCode 값과 같으면 변경하지 않는다")
    void execute_does_not_update_tokenLimit_when_already_same_as_entry_code() {
        // given
        Long examId = 6L;
        final int sameTokenLimit = 50000;

        ExamParticipant participant = ExamParticipant.builder()
                .examId(examId)
                .participantId(6L)
                .tokenLimit(sameTokenLimit)
                .build();
        ReflectionTestUtils.setField(participant, "id", 6L);

        EntryCode activeCode = EntryCode.builder()
                .code("CODE-SAME")
                .examId(examId)
                .tokenLimit(sameTokenLimit)
                .isActive(true)
                .maxUses(0)
                .build();

        Exam exam = Exam.builder()
                .title("시험 6")
                .state(ExamState.RUNNING)
                .startsAt(LocalDateTime.now())
                .endsAt(LocalDateTime.now().plusHours(1))
                .version(1)
                .createdBy(1L)
                .build();

        given(examParticipantRepository.findByExamId(examId)).willReturn(List.of(participant));
        given(entryCodeRepository.findTopByExamIdAndIsActiveOrderByCreatedAtDesc(examId, true)).willReturn(Optional.of(activeCode));
        given(examService.startExam(examId)).willReturn(exam);

        // when
        startExamUseCase.execute(examId);

        // then — 동일 값이므로 tokenLimit 이 변하지 않아야 함
        assertThat(participant.getTokenLimit()).isEqualTo(sameTokenLimit);
    }

    // -------------------------------------------------------------------------
    // 3. 복합 시나리오 — specId + tokenLimit 동시 동기화
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("시험 시작 시 specId와 tokenLimit이 동시에 동기화된다")
    void execute_syncs_both_specId_and_tokenLimit_simultaneously() {
        // given
        Long examId = 7L;
        Long problemId = 12L;
        Long newSpecId = 300L;
        final int newTokenLimit = 60000;

        ExamParticipant participant = ExamParticipant.builder()
                .examId(examId)
                .participantId(7L)
                .assignedProblemId(problemId)
                .specId(null)       // 구버전 — specId 미설정
                .tokenLimit(20000)  // 구버전 기본값
                .build();
        ReflectionTestUtils.setField(participant, "id", 7L);

        Problem problem = Problem.builder()
                .title("문제 C")
                .difficulty(Difficulty.HARD)
                .status(ProblemStatus.PUBLISHED)
                .currentSpecId(newSpecId)
                .build();
        ReflectionTestUtils.setField(problem, "id", problemId);

        EntryCode activeCode = EntryCode.builder()
                .code("CODE-COMBO")
                .examId(examId)
                .tokenLimit(newTokenLimit)
                .isActive(true)
                .maxUses(0)
                .build();

        Exam exam = Exam.builder()
                .title("복합 시험")
                .state(ExamState.RUNNING)
                .startsAt(LocalDateTime.now())
                .endsAt(LocalDateTime.now().plusHours(3))
                .version(1)
                .createdBy(1L)
                .build();

        given(examParticipantRepository.findByExamId(examId)).willReturn(List.of(participant));
        given(entryCodeRepository.findTopByExamIdAndIsActiveOrderByCreatedAtDesc(examId, true)).willReturn(Optional.of(activeCode));
        given(problemRepository.findAllById(Set.of(problemId))).willReturn(List.of(problem));
        given(examService.startExam(examId)).willReturn(exam);

        // when
        startExamUseCase.execute(examId);

        // then — 두 값 모두 갱신됨
        assertThat(participant.getSpecId()).isEqualTo(newSpecId);
        assertThat(participant.getTokenLimit()).isEqualTo(newTokenLimit);
    }

    // -------------------------------------------------------------------------
    // 4. 엣지 케이스 — 참가자가 없을 때 syncParticipants 가 조용히 종료된다
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("시험에 참가자가 없어도 시험 시작이 정상적으로 완료된다")
    void execute_succeeds_when_no_participants() {
        // given
        Long examId = 8L;

        Exam exam = Exam.builder()
                .title("빈 시험")
                .state(ExamState.RUNNING)
                .startsAt(LocalDateTime.now())
                .endsAt(LocalDateTime.now().plusHours(1))
                .version(1)
                .createdBy(1L)
                .build();

        given(examParticipantRepository.findByExamId(examId)).willReturn(Collections.emptyList());
        given(examService.startExam(examId)).willReturn(exam);

        // when
        startExamUseCase.execute(examId);

        // then — EntryCode 조회 없이도 시험 시작 및 WS 브로드캐스트가 이루어져야 함
        verify(entryCodeRepository, never()).findByExamIdAndIsActive(any(), any());
        verify(examService).startExam(examId);
        verify(messagingTemplate).convertAndSend(eq("/topic/exam/" + examId), any(ExamStateEvent.class));
    }

    @Test
    @DisplayName("여러 참가자가 있을 때 모든 참가자의 specId가 동기화된다")
    void execute_syncs_specId_for_all_participants() {
        // given
        Long examId = 9L;
        Long problemId = 13L;
        Long latestSpecId = 400L;

        ExamParticipant participant1 = ExamParticipant.builder()
                .examId(examId).participantId(10L)
                .assignedProblemId(problemId).specId(1L).tokenLimit(20000).build();
        ReflectionTestUtils.setField(participant1, "id", 10L);

        ExamParticipant participant2 = ExamParticipant.builder()
                .examId(examId).participantId(11L)
                .assignedProblemId(problemId).specId(2L).tokenLimit(20000).build();
        ReflectionTestUtils.setField(participant2, "id", 11L);

        Problem problem = Problem.builder()
                .title("문제 D")
                .difficulty(Difficulty.EASY)
                .status(ProblemStatus.PUBLISHED)
                .currentSpecId(latestSpecId)
                .build();
        ReflectionTestUtils.setField(problem, "id", problemId);

        Exam exam = Exam.builder()
                .title("다중 참가 시험")
                .state(ExamState.RUNNING)
                .startsAt(LocalDateTime.now())
                .endsAt(LocalDateTime.now().plusHours(1))
                .version(1).createdBy(1L).build();

        given(examParticipantRepository.findByExamId(examId)).willReturn(List.of(participant1, participant2));
        given(entryCodeRepository.findTopByExamIdAndIsActiveOrderByCreatedAtDesc(examId, true)).willReturn(Optional.empty());
        given(problemRepository.findAllById(Set.of(problemId))).willReturn(List.of(problem));
        given(examService.startExam(examId)).willReturn(exam);

        // when
        startExamUseCase.execute(examId);

        // then — 두 참가자 모두 최신 specId 로 갱신됐어야 함
        assertThat(participant1.getSpecId()).isEqualTo(latestSpecId);
        assertThat(participant2.getSpecId()).isEqualTo(latestSpecId);
    }
}
