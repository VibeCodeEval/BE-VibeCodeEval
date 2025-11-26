package com.yd.vibecode.domain.auth.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.yd.vibecode.domain.auth.domain.entity.Participant;
import com.yd.vibecode.domain.auth.domain.repository.ParticipantRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ParticipantServiceTest {

    @InjectMocks
    private ParticipantService participantService;

    @Mock
    private ParticipantRepository participantRepository;

    @Test
    @DisplayName("참가자 생성 성공")
    void create_success() {
        // given
        String name = "홍길동";
        String phone = "010-1234-5678";
        Participant participant = Participant.builder()
                .name(name)
                .phone(phone)
                .build();

        given(participantRepository.save(any(Participant.class))).willReturn(participant);

        // when
        Participant result = participantService.create(name, phone);

        // then
        assertThat(result.getName()).isEqualTo(name);
        verify(participantRepository).save(any(Participant.class));
    }

    @Test
    @DisplayName("전화번호로 참가자 조회")
    void findByPhone_success() {
        // given
        String phone = "010-1234-5678";
        Participant participant = Participant.builder().phone(phone).build();
        given(participantRepository.findByPhone(phone)).willReturn(Optional.of(participant));

        // when
        Participant result = participantService.findByPhone(phone);

        // then
        assertThat(result.getPhone()).isEqualTo(phone);
    }
}
