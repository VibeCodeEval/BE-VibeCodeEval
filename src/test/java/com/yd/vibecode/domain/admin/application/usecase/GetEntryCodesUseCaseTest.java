package com.yd.vibecode.domain.admin.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yd.vibecode.domain.admin.application.dto.response.EntryCodeResponse;
import com.yd.vibecode.domain.auth.domain.entity.EntryCode;
import com.yd.vibecode.domain.auth.domain.repository.EntryCodeRepository;

@ExtendWith(MockitoExtension.class)
class GetEntryCodesUseCaseTest {

    @Mock
    private EntryCodeRepository entryCodeRepository;

    @InjectMocks
    private GetEntryCodesUseCase getEntryCodesUseCase;

    @Test
    @DisplayName("examId로 모든 입장 코드 조회 성공")
    void execute_withExamIdOnly_success() {
        // given
        Long examId = 1L;
        EntryCode entryCode = EntryCode.builder()
            .code("TEST-CODE")
            .examId(examId)
            .problemSetId(100L)
            .createdBy(1L)
            .label("Test Label")
            .expiresAt(LocalDateTime.now().plusDays(1))
            .maxUses(10)
            .usedCount(0)
            .isActive(true)
            .build();

        given(entryCodeRepository.findByExamId(examId))
            .willReturn(List.of(entryCode));

        // when
        List<EntryCodeResponse> result = getEntryCodesUseCase.execute(examId, null);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).code()).isEqualTo("TEST-CODE");
        assertThat(result.get(0).examId()).isEqualTo(examId);
        verify(entryCodeRepository).findByExamId(examId);
    }

    @Test
    @DisplayName("examId와 isActive로 입장 코드 필터링 조회 성공")
    void execute_withExamIdAndIsActive_success() {
        // given
        Long examId = 1L;
        Boolean isActive = true;
        EntryCode entryCode = EntryCode.builder()
            .code("ACTIVE-CODE")
            .examId(examId)
            .problemSetId(100L)
            .createdBy(1L)
            .label("Active Label")
            .expiresAt(LocalDateTime.now().plusDays(1))
            .maxUses(10)
            .usedCount(0)
            .isActive(true)
            .build();

        given(entryCodeRepository.findByExamIdAndIsActive(examId, isActive))
            .willReturn(List.of(entryCode));

        // when
        List<EntryCodeResponse> result = getEntryCodesUseCase.execute(examId, isActive);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).code()).isEqualTo("ACTIVE-CODE");
        assertThat(result.get(0).isActive()).isTrue();
        verify(entryCodeRepository).findByExamIdAndIsActive(examId, isActive);
    }

    @Test
    @DisplayName("조회 결과가 없을 때 빈 리스트 반환")
    void execute_noResults_returnsEmptyList() {
        // given
        Long examId = 1L;
        given(entryCodeRepository.findByExamId(examId))
            .willReturn(List.of());

        // when
        List<EntryCodeResponse> result = getEntryCodesUseCase.execute(examId, null);

        // then
        assertThat(result).isEmpty();
    }
}
