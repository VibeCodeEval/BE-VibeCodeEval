package com.yd.vibecode.domain.auth.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.yd.vibecode.domain.auth.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.auth.domain.repository.ExamParticipantRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExamParticipantServiceTest {

    @InjectMocks
    private ExamParticipantService examParticipantService;

    @Mock
    private ExamParticipantRepository examParticipantRepository;

    @Test
    @DisplayName("시험 참가자 생성 성공")
    void create_success() {
        // given
        Long examId = 1L;
        Long participantId = 100L;
        ExamParticipant examParticipant = ExamParticipant.builder()
                .examId(examId)
                .participantId(participantId)
                .build();

        given(examParticipantRepository.save(any(ExamParticipant.class))).willReturn(examParticipant);

        // when
        ExamParticipant result = examParticipantService.create(examId, participantId, null, 20000);

        // then
        assertThat(result.getExamId()).isEqualTo(examId);
        verify(examParticipantRepository).save(any(ExamParticipant.class));
    }

    @Test
    @DisplayName("시험 ID와 참가자 ID로 조회")
    void findByExamIdAndParticipantId_success() {
        // given
        Long examId = 1L;
        Long participantId = 100L;
        ExamParticipant examParticipant = ExamParticipant.builder().examId(examId).participantId(participantId).build();
        given(examParticipantRepository.findByExamIdAndParticipantId(examId, participantId)).willReturn(Optional.of(examParticipant));

        // when
        ExamParticipant result = examParticipantService.findByExamIdAndParticipantId(examId, participantId);

        // then
        assertThat(result.getExamId()).isEqualTo(examId);
    }
}
