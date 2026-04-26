package com.yd.vibecode.domain.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.yd.vibecode.domain.auth.application.dto.request.EnterRequest;
import com.yd.vibecode.domain.auth.application.dto.response.EnterResponse;
import com.yd.vibecode.domain.auth.domain.entity.EntryCode;
import com.yd.vibecode.domain.auth.domain.entity.User;
import com.yd.vibecode.domain.auth.domain.service.EntryCodeService;
import com.yd.vibecode.domain.auth.domain.service.UserService;
import com.yd.vibecode.domain.exam.domain.entity.Exam;
import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.exam.domain.entity.ExamState;
import com.yd.vibecode.domain.exam.domain.repository.ExamParticipantRepository;
import com.yd.vibecode.domain.exam.domain.service.ExamParticipantService;
import com.yd.vibecode.domain.exam.domain.service.ExamService;
import com.yd.vibecode.domain.problem.domain.entity.Difficulty;
import com.yd.vibecode.domain.problem.domain.entity.Problem;
import com.yd.vibecode.domain.problem.domain.entity.ProblemStatus;
import com.yd.vibecode.domain.problem.domain.service.ProblemService;
import com.yd.vibecode.domain.problem.infrastructure.entity.ProblemSetItem;
import com.yd.vibecode.domain.problem.infrastructure.repository.ProblemSetItemRepository;
import com.yd.vibecode.global.security.TokenProvider;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * EnterUseCase 버그픽스 검증 테스트
 *
 * <p>버그: 대기 중에 입장 시 tokenLimit 이 항상 구버전 기본값(20000)으로 고정되던 문제
 * <p>수정: EntryCode 에 tokenLimit 전용 필드를 두고, 신규 참가자 생성 시
 *         entryCode.getTokenLimit() 값을 ExamParticipantService.create() 에 전달
 */
@ExtendWith(MockitoExtension.class)
class EnterUseCaseTokenLimitTest {

    @InjectMocks
    private EnterUseCase enterUseCase;

    @Mock
    private EntryCodeService entryCodeService;
    @Mock
    private UserService userService;
    @Mock
    private ExamParticipantService examParticipantService;
    @Mock
    private ExamParticipantRepository examParticipantRepository;
    @Mock
    private TokenProvider tokenProvider;
    @Mock
    private ExamService examService;
    @Mock
    private ProblemSetItemRepository problemSetItemRepository;
    @Mock
    private ProblemService problemService;

    // -------------------------------------------------------------------------
    // 1. 신규 참가자 — entryCode.getTokenLimit() 이 그대로 전달되는지 검증
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("신규 참가자 입장 시 EntryCode의 tokenLimit(50000)이 ExamParticipant에 적용된다")
    void enter_newParticipant_uses_entryCode_tokenLimit() {
        // given
        final int customTokenLimit = 50000;

        EntryCode entryCode = EntryCode.builder()
                .code("CODE-CUSTOM")
                .examId(1L)
                .tokenLimit(customTokenLimit)
                .maxUses(0)
                .build();

        User newUser = User.builder()
                .name("신규사용자")
                .phone("010-0000-1111")
                .build();
        ReflectionTestUtils.setField(newUser, "id", 200L);

        ExamParticipant created = ExamParticipant.builder()
                .examId(1L)
                .participantId(200L)
                .tokenLimit(customTokenLimit)
                .tokenUsed(0)
                .build();
        ReflectionTestUtils.setField(created, "id", 300L);

        Exam exam = Exam.builder()
                .title("테스트 시험")
                .state(ExamState.WAITING)
                .startsAt(LocalDateTime.now())
                .endsAt(LocalDateTime.now().plusHours(2))
                .build();
        ReflectionTestUtils.setField(exam, "id", 1L);

        given(entryCodeService.findByCode("CODE-CUSTOM")).willReturn(entryCode);
        given(userService.findByPhone("010-0000-1111")).willReturn(null);
        given(userService.create("신규사용자", "010-0000-1111")).willReturn(newUser);
        given(examParticipantService.findByExamIdAndParticipantId(1L, 200L)).willReturn(null);
        given(problemSetItemRepository.findByProblemSetId(null)).willReturn(Collections.emptyList());
        // create() 가 customTokenLimit 으로 호출돼야 함
        given(examParticipantService.create(eq(1L), eq(200L), eq(null), eq(customTokenLimit), eq(null)))
                .willReturn(created);
        given(tokenProvider.createAccessToken(anyString(), anyString())).willReturn("token");
        given(examService.findById(1L)).willReturn(exam);

        // when
        EnterResponse response = enterUseCase.execute(new EnterRequest("CODE-CUSTOM", "신규사용자", "010-0000-1111"));

        // then — 응답의 session.tokenLimit 이 EntryCode 의 값과 일치해야 한다
        assertThat(response.session().tokenLimit()).isEqualTo(customTokenLimit);

        // ExamParticipantService.create() 에 entryCode.getTokenLimit() 값이 전달됐는지 명시적으로 검증
        verify(examParticipantService).create(eq(1L), eq(200L), eq(null), eq(customTokenLimit), eq(null));
    }

    @Test
    @DisplayName("신규 참가자 입장 시 구버전 기본값(20000)이 아닌 EntryCode의 tokenLimit(100000)이 사용된다")
    void enter_newParticipant_does_not_use_legacy_default_tokenLimit() {
        // given — 구버전 기본값과 명확히 다른 값으로 설정
        final int legacyDefault = 20000;
        final int newTokenLimit = 100000;

        assertThat(newTokenLimit).isNotEqualTo(legacyDefault); // 전제 조건 확인

        EntryCode entryCode = EntryCode.builder()
                .code("CODE-NEW")
                .examId(2L)
                .tokenLimit(newTokenLimit)
                .maxUses(5)
                .build();

        User newUser = User.builder()
                .name("홍길동")
                .phone("010-1111-2222")
                .build();
        ReflectionTestUtils.setField(newUser, "id", 201L);

        ExamParticipant created = ExamParticipant.builder()
                .examId(2L)
                .participantId(201L)
                .tokenLimit(newTokenLimit)
                .tokenUsed(0)
                .build();
        ReflectionTestUtils.setField(created, "id", 301L);

        Exam exam = Exam.builder()
                .title("고용량 시험")
                .state(ExamState.WAITING)
                .startsAt(LocalDateTime.now())
                .endsAt(LocalDateTime.now().plusHours(3))
                .build();
        ReflectionTestUtils.setField(exam, "id", 2L);

        given(entryCodeService.findByCode("CODE-NEW")).willReturn(entryCode);
        given(userService.findByPhone("010-1111-2222")).willReturn(null);
        given(userService.create("홍길동", "010-1111-2222")).willReturn(newUser);
        given(examParticipantService.findByExamIdAndParticipantId(2L, 201L)).willReturn(null);
        given(problemSetItemRepository.findByProblemSetId(null)).willReturn(Collections.emptyList());
        given(examParticipantService.create(eq(2L), eq(201L), eq(null), eq(newTokenLimit), eq(null)))
                .willReturn(created);
        given(tokenProvider.createAccessToken(anyString(), anyString())).willReturn("token2");
        given(examService.findById(2L)).willReturn(exam);

        // when
        EnterResponse response = enterUseCase.execute(new EnterRequest("CODE-NEW", "홍길동", "010-1111-2222"));

        // then — 절대로 구버전 기본값(20000) 이 아니어야 한다
        assertThat(response.session().tokenLimit())
                .isNotEqualTo(legacyDefault)
                .isEqualTo(newTokenLimit);
    }

    // -------------------------------------------------------------------------
    // 2. 신규 참가자 + 문제 세트 존재 — specId 와 tokenLimit 모두 EntryCode 기준
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("신규 참가자 입장 시 문제 currentSpecId와 EntryCode tokenLimit이 함께 전달된다")
    void enter_newParticipant_with_problemSet_applies_specId_and_entryCode_tokenLimit() {
        // given
        final int customTokenLimit = 75000;
        final long problemSetId = 10L;
        final long problemId = 20L;
        final long expectedSpecId = 30L;

        EntryCode entryCode = EntryCode.builder()
                .code("CODE-PS")
                .examId(3L)
                .problemSetId(problemSetId)
                .tokenLimit(customTokenLimit)
                .maxUses(0)
                .build();

        User newUser = User.builder()
                .name("이영희")
                .phone("010-3333-4444")
                .build();
        ReflectionTestUtils.setField(newUser, "id", 202L);

        ProblemSetItem item = ProblemSetItem.builder()
                .problemSetId(problemSetId)
                .problemId(problemId)
                .build();

        Problem problem = Problem.builder()
                .title("알고리즘 문제")
                .difficulty(Difficulty.MEDIUM)
                .status(ProblemStatus.PUBLISHED)
                .currentSpecId(expectedSpecId)
                .build();
        ReflectionTestUtils.setField(problem, "id", problemId);

        ExamParticipant created = ExamParticipant.builder()
                .examId(3L)
                .participantId(202L)
                .specId(expectedSpecId)
                .assignedProblemId(problemId)
                .tokenLimit(customTokenLimit)
                .tokenUsed(0)
                .build();
        ReflectionTestUtils.setField(created, "id", 302L);

        Exam exam = Exam.builder()
                .title("문제세트 시험")
                .state(ExamState.WAITING)
                .startsAt(LocalDateTime.now())
                .endsAt(LocalDateTime.now().plusHours(2))
                .build();
        ReflectionTestUtils.setField(exam, "id", 3L);

        given(entryCodeService.findByCode("CODE-PS")).willReturn(entryCode);
        given(userService.findByPhone("010-3333-4444")).willReturn(null);
        given(userService.create("이영희", "010-3333-4444")).willReturn(newUser);
        given(examParticipantService.findByExamIdAndParticipantId(3L, 202L)).willReturn(null);
        given(problemSetItemRepository.findByProblemSetId(problemSetId)).willReturn(List.of(item));
        given(problemService.findById(problemId)).willReturn(problem);
        // specId = expectedSpecId, tokenLimit = customTokenLimit (EntryCode 값)
        given(examParticipantService.create(
                eq(3L), eq(202L), eq(expectedSpecId), eq(customTokenLimit), eq(problemId)))
                .willReturn(created);
        given(tokenProvider.createAccessToken(anyString(), anyString())).willReturn("token3");
        given(examService.findById(3L)).willReturn(exam);

        // when
        EnterResponse response = enterUseCase.execute(new EnterRequest("CODE-PS", "이영희", "010-3333-4444"));

        // then
        assertThat(response.session().tokenLimit()).isEqualTo(customTokenLimit);
        verify(examParticipantService).create(
                eq(3L), eq(202L), eq(expectedSpecId), eq(customTokenLimit), eq(problemId));
    }

    // -------------------------------------------------------------------------
    // 3. EntryCode.tokenLimit 기본값 20000 — 값 미지정 시 필드 기본값 사용
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("EntryCode tokenLimit 미설정 시 기본값(20000)이 유지된다")
    void entryCode_tokenLimit_default_is_20000_when_not_specified() {
        // given — tokenLimit 을 명시하지 않고 빌더 사용
        EntryCode entryCode = EntryCode.builder()
                .code("CODE-DEFAULT")
                .examId(4L)
                .maxUses(0)
                .build();

        // then
        assertThat(entryCode.getTokenLimit()).isEqualTo(20000);
    }

    @Test
    @DisplayName("EntryCode 빌더에서 tokenLimit 명시 시 해당 값이 저장된다")
    void entryCode_tokenLimit_set_explicitly_is_stored_correctly() {
        // given
        EntryCode entryCode = EntryCode.builder()
                .code("CODE-EXPLICIT")
                .examId(5L)
                .tokenLimit(80000)
                .maxUses(0)
                .build();

        // then
        assertThat(entryCode.getTokenLimit()).isEqualTo(80000);
    }

    @Test
    @DisplayName("EntryCode.updateTokenLimit — 양수 값으로만 갱신된다")
    void entryCode_updateTokenLimit_only_accepts_positive_value() {
        // given
        EntryCode entryCode = EntryCode.builder()
                .code("CODE-UPD")
                .examId(6L)
                .tokenLimit(30000)
                .maxUses(0)
                .build();

        // when — 유효한 값으로 갱신
        entryCode.updateTokenLimit(60000);

        // then
        assertThat(entryCode.getTokenLimit()).isEqualTo(60000);

        // when — 0 이하 값은 무시됨
        entryCode.updateTokenLimit(0);
        entryCode.updateTokenLimit(-1);

        // then — 여전히 60000 유지
        assertThat(entryCode.getTokenLimit()).isEqualTo(60000);
    }

    // -------------------------------------------------------------------------
    // 4. 기존 참가자 재입장 — tokenLimit 은 기존 ExamParticipant 값을 그대로 사용
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("기존 참가자 재입장 시 EntryCode tokenLimit과 관계없이 기존 세션 tokenLimit을 반환한다")
    void enter_existingParticipant_returns_existing_tokenLimit() {
        // given
        final int entryCodeTokenLimit = 99000;
        final int existingTokenLimit = 45000; // 기존 참가자 세션에 이미 설정된 값

        EntryCode entryCode = EntryCode.builder()
                .code("CODE-EXIST")
                .examId(7L)
                .tokenLimit(entryCodeTokenLimit)
                .maxUses(0)
                .build();

        User existingUser = User.builder()
                .name("김유저")
                .phone("010-5555-6666")
                .build();
        ReflectionTestUtils.setField(existingUser, "id", 203L);

        ExamParticipant existingParticipant = ExamParticipant.builder()
                .examId(7L)
                .participantId(203L)
                .tokenLimit(existingTokenLimit)
                .tokenUsed(1000)
                .build();
        ReflectionTestUtils.setField(existingParticipant, "id", 303L);

        Exam exam = Exam.builder()
                .title("재입장 시험")
                .state(ExamState.WAITING)
                .startsAt(LocalDateTime.now())
                .endsAt(LocalDateTime.now().plusHours(1))
                .build();
        ReflectionTestUtils.setField(exam, "id", 7L);

        given(entryCodeService.findByCode("CODE-EXIST")).willReturn(entryCode);
        given(userService.findByPhone("010-5555-6666")).willReturn(existingUser);
        given(examParticipantService.findByExamIdAndParticipantId(7L, 203L)).willReturn(existingParticipant);
        given(tokenProvider.createAccessToken(anyString(), anyString())).willReturn("token-exist");
        given(examService.findById(7L)).willReturn(exam);

        // when
        EnterResponse response = enterUseCase.execute(new EnterRequest("CODE-EXIST", "김유저", "010-5555-6666"));

        // then — 기존 참가자는 새로 create() 하지 않으므로 기존 tokenLimit 이 반환됨
        assertThat(response.session().tokenLimit()).isEqualTo(existingTokenLimit);
    }
}
