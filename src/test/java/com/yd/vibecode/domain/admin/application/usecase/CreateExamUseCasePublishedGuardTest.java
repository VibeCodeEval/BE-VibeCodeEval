package com.yd.vibecode.domain.admin.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yd.vibecode.domain.auth.domain.repository.EntryCodeRepository;
import com.yd.vibecode.domain.exam.application.dto.request.CreateExamRequest;
import com.yd.vibecode.domain.exam.domain.repository.ExamRepository;
import com.yd.vibecode.domain.problem.domain.entity.ProblemStatus;
import com.yd.vibecode.domain.problem.domain.repository.ProblemRepository;
import com.yd.vibecode.domain.problem.infrastructure.repository.ProblemSetItemRepository;
import com.yd.vibecode.domain.problem.infrastructure.repository.ProblemSetRepository;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.ProblemErrorStatus;

@ExtendWith(MockitoExtension.class)
class CreateExamUseCasePublishedGuardTest {

    @InjectMocks
    private CreateExamUseCase createExamUseCase;

    @Mock
    private ExamRepository examRepository;

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private ProblemSetRepository problemSetRepository;

    @Mock
    private ProblemSetItemRepository problemSetItemRepository;

    @Mock
    private EntryCodeRepository entryCodeRepository;

    @Test
    @DisplayName("PUBLISHED 문제가 없으면 NO_PUBLISHED_PROBLEMS로 시험 생성 실패")
    void execute_noPublishedProblems_throws() {
        CreateExamRequest request = new CreateExamRequest(
                "테스트 시험",
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3)
        );
        given(problemRepository.findByStatus(ProblemStatus.PUBLISHED)).willReturn(Collections.emptyList());

        assertThatThrownBy(() -> createExamUseCase.execute(1L, request))
                .isInstanceOf(RestApiException.class)
                .extracting(ex -> ((RestApiException) ex).getErrorCode().getCode())
                .isEqualTo(ProblemErrorStatus.NO_PUBLISHED_PROBLEMS.getCode().getCode());

        verify(problemSetRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(entryCodeRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
