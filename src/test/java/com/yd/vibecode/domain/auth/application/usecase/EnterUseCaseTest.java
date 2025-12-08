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
import com.yd.vibecode.domain.problem.infrastructure.repository.ProblemSetItemRepository;
import com.yd.vibecode.global.security.TokenProvider;
import java.time.LocalDateTime;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EnterUseCaseTest {

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

    @Test
    @DisplayName("입장 성공 - 기존 참가자")
    void enter_success_existing_participant() {
        // given
        EnterRequest request = new EnterRequest("CODE123", "홍길동", "010-1234-5678");
        EntryCode entryCode = EntryCode.builder()
                .code("CODE123")
                .examId(1L)
                .maxUses(0)
                .build();
        
        User participant = User.builder()
                .name("홍길동")
                .phone("010-1234-5678")
                .build();
        ReflectionTestUtils.setField(participant, "id", 100L);

        ExamParticipant examParticipant = ExamParticipant.builder()
                .examId(1L)
                .participantId(100L)
                .tokenLimit(20000)
                .tokenUsed(0)
                .build();
        ReflectionTestUtils.setField(examParticipant, "id", 200L);

        Exam exam = Exam.builder()
                .title("Test Exam")
                .state(ExamState.WAITING)
                .startsAt(LocalDateTime.now())
                .endsAt(LocalDateTime.now().plusHours(1))
                .build();
        ReflectionTestUtils.setField(exam, "id", 1L);

        given(entryCodeService.findByCode("CODE123")).willReturn(entryCode);
        given(userService.findByPhone("010-1234-5678")).willReturn(participant);
        given(examParticipantService.findByExamIdAndParticipantId(1L, 100L)).willReturn(examParticipant);
        given(tokenProvider.createAccessToken(anyString(), anyString())).willReturn("accessToken");
        given(examService.findById(1L)).willReturn(exam);

        // when
        EnterResponse response = enterUseCase.execute(request);

        // then
        assertThat(response.accessToken()).isEqualTo("accessToken");
        assertThat(response.participant().name()).isEqualTo("홍길동");
        assertThat(response.exam().id()).isEqualTo(1L);
        verify(entryCodeService).validateEntryCode(entryCode);
        verify(entryCodeService).incrementUsedCount(entryCode);
    }

    @Test
    @DisplayName("입장 성공 - 신규 참가자")
    void enter_success_new_participant() {
        // given
        EnterRequest request = new EnterRequest("CODE123", "김철수", "010-9876-5432");
        EntryCode entryCode = EntryCode.builder()
                .code("CODE123")
                .examId(1L)
                .maxUses(10)
                .build();

        User newParticipant = User.builder()
                .name("김철수")
                .phone("010-9876-5432")
                .build();
        ReflectionTestUtils.setField(newParticipant, "id", 101L);

        ExamParticipant newExamParticipant = ExamParticipant.builder()
                .examId(1L)
                .participantId(101L)
                .tokenLimit(10000) // maxUses * 1000
                .build();
        ReflectionTestUtils.setField(newExamParticipant, "id", 201L);

        Exam exam = Exam.builder()
                .title("Test Exam")
                .state(ExamState.WAITING)
                .startsAt(LocalDateTime.now())
                .endsAt(LocalDateTime.now().plusHours(1))
                .build();
        ReflectionTestUtils.setField(exam, "id", 1L);

        given(entryCodeService.findByCode("CODE123")).willReturn(entryCode);
        given(userService.findByPhone("010-9876-5432")).willReturn(null);
        given(userService.create("김철수", "010-9876-5432")).willReturn(newParticipant);
        given(examParticipantService.findByExamIdAndParticipantId(1L, 101L)).willReturn(null);
        given(problemSetItemRepository.findByProblemSetId(null)).willReturn(Collections.emptyList());
        given(examParticipantService.create(eq(1L), eq(101L), eq(null), eq(10000), eq(null))).willReturn(newExamParticipant);
        given(tokenProvider.createAccessToken(anyString(), anyString())).willReturn("accessToken");
        given(examService.findById(1L)).willReturn(exam);

        // when
        EnterResponse response = enterUseCase.execute(request);

        // then
        assertThat(response.accessToken()).isEqualTo("accessToken");
        assertThat(response.participant().name()).isEqualTo("김철수");
        assertThat(response.session().tokenLimit()).isEqualTo(10000);
        verify(userService).create("김철수", "010-9876-5432");
        verify(examParticipantService).create(eq(1L), eq(101L), eq(null), eq(10000), eq(null));
    }
}
