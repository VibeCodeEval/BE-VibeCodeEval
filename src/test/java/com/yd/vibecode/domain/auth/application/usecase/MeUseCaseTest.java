package com.yd.vibecode.domain.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.yd.vibecode.domain.auth.application.dto.response.MeResponse;
import com.yd.vibecode.domain.auth.domain.entity.User;
import com.yd.vibecode.domain.auth.domain.service.UserService;
import com.yd.vibecode.domain.exam.domain.entity.Exam;
import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.exam.domain.entity.ExamState;
import com.yd.vibecode.domain.exam.domain.service.ExamParticipantService;
import com.yd.vibecode.domain.exam.domain.service.ExamService;
import com.yd.vibecode.global.security.TokenProvider;
import java.time.LocalDateTime;
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
    private UserService userService;
    @Mock
    private ExamParticipantService examParticipantService;
    @Mock
    private ExamService examService;

    @Test
    @DisplayName("내 정보 조회 성공")
    void me_success() {
        // given
        String token = "accessToken";
        Long participantId = 100L;
        
        User participant = User.builder()
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

        Exam exam = Exam.builder()
                .title("Test Exam")
                .state(ExamState.WAITING)
                .startsAt(LocalDateTime.now())
                .endsAt(LocalDateTime.now().plusHours(1))
                .build();
        ReflectionTestUtils.setField(exam, "id", 1L);

        given(tokenProvider.getId(token)).willReturn(Optional.of(String.valueOf(participantId)));
        given(tokenProvider.getRole(token)).willReturn(Optional.of("USER"));
        given(userService.findById(participantId)).willReturn(participant);
        given(examParticipantService.findLatestByParticipantId(participantId)).willReturn(examParticipant);
        given(examService.findById(1L)).willReturn(exam);

        // when
        MeResponse response = meUseCase.execute(token);

        // then
        assertThat(response.role()).isEqualTo("USER");
        assertThat(response.participant().name()).isEqualTo("홍길동");
        assertThat(response.exam().state()).isEqualTo("WAITING");
        assertThat(response.session().tokenUsed()).isEqualTo(500);
    }
}
