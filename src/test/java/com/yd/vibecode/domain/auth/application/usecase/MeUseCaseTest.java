package com.yd.vibecode.domain.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.yd.vibecode.domain.auth.application.dto.response.MeResponse;
import com.yd.vibecode.domain.auth.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.auth.domain.entity.Participant;
import com.yd.vibecode.domain.auth.domain.service.ExamParticipantService;
import com.yd.vibecode.domain.auth.domain.service.ParticipantService;
import com.yd.vibecode.global.security.TokenProvider;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MeUseCaseTest {

    @InjectMocks
    private MeUseCase meUseCase;

    @Mock
    private TokenProvider tokenProvider;
    @Mock
    private ParticipantService participantService;
    @Mock
    private ExamParticipantService examParticipantService;

    @Test
    @DisplayName("내 정보 조회 성공")
    void me_success() {
        // given
        String token = "accessToken";
        Long participantId = 100L;
        
        Participant participant = Participant.builder()
                .name("홍길동")
                .phone("010-1234-5678")
                .build();
        ReflectionTestUtils.setField(participant, "id", participantId);

        ExamParticipant examParticipant = ExamParticipant.builder()
                .examId(1L)
                .participantId(participantId)
                .tokenLimit(20000)
                .tokenUsed(500)
                .state("WAITING")
                .build();
        ReflectionTestUtils.setField(examParticipant, "id", 200L);

        given(tokenProvider.getId(token)).willReturn(Optional.of(String.valueOf(participantId)));
        given(tokenProvider.getRole(token)).willReturn(Optional.of("USER"));
        given(participantService.findById(participantId)).willReturn(participant);
        given(examParticipantService.findLatestByParticipantId(participantId)).willReturn(examParticipant);

        // when
        MeResponse response = meUseCase.execute(token);

        // then
        assertThat(response.role()).isEqualTo("USER");
        assertThat(response.participant().name()).isEqualTo("홍길동");
        assertThat(response.exam().state()).isEqualTo("WAITING");
        assertThat(response.session().tokenUsed()).isEqualTo(500);
    }
}
