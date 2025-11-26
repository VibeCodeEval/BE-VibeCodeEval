package com.yd.vibecode.domain.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

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
    private ParticipantService participantService;
    @Mock
    private ExamParticipantService examParticipantService;
    @Mock
    private ExamParticipantRepository examParticipantRepository;
    @Mock
    private TokenProvider tokenProvider;

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
        
        Participant participant = Participant.builder()
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

        given(entryCodeService.findByCode("CODE123")).willReturn(entryCode);
        given(participantService.findByPhone("010-1234-5678")).willReturn(participant);
        given(examParticipantService.findByExamIdAndParticipantId(1L, 100L)).willReturn(examParticipant);
        given(tokenProvider.createAccessToken(anyString(), anyString())).willReturn("accessToken");

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

        Participant newParticipant = Participant.builder()
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

        given(entryCodeService.findByCode("CODE123")).willReturn(entryCode);
        given(participantService.findByPhone("010-9876-5432")).willReturn(null);
        given(participantService.create("김철수", "010-9876-5432")).willReturn(newParticipant);
        given(examParticipantService.findByExamIdAndParticipantId(1L, 101L)).willReturn(null);
        given(examParticipantService.create(eq(1L), eq(101L), eq(null), eq(10000))).willReturn(newExamParticipant);
        given(tokenProvider.createAccessToken(anyString(), anyString())).willReturn("accessToken");

        // when
        EnterResponse response = enterUseCase.execute(request);

        // then
        assertThat(response.accessToken()).isEqualTo("accessToken");
        assertThat(response.participant().name()).isEqualTo("김철수");
        assertThat(response.session().tokenLimit()).isEqualTo(10000);
        verify(participantService).create("김철수", "010-9876-5432");
        verify(examParticipantService).create(eq(1L), eq(101L), eq(null), eq(10000));
    }
}
