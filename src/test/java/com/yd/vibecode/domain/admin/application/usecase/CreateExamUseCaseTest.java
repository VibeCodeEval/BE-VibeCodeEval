package com.yd.vibecode.domain.admin.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yd.vibecode.domain.auth.domain.repository.EntryCodeRepository;
import com.yd.vibecode.domain.exam.application.dto.request.CreateExamRequest;
import com.yd.vibecode.domain.exam.domain.repository.ExamRepository;
import com.yd.vibecode.domain.problem.domain.repository.ProblemRepository;
import com.yd.vibecode.domain.problem.infrastructure.repository.ProblemSetItemRepository;
import com.yd.vibecode.domain.problem.infrastructure.repository.ProblemSetRepository;
import com.yd.vibecode.domain.admin.domain.service.AdminActivityLogService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.ExamErrorStatus;

@ExtendWith(MockitoExtension.class)
class CreateExamUseCaseTest {

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

    @Mock
    private AdminActivityLogService adminActivityLogService;

    @Test
    @DisplayName("종료 시각이 과거이면 시험 생성 실패 (DB 저장 없음)")
    void execute_pastEndsAt_throwsBeforeSave() {
        CreateExamRequest request = new CreateExamRequest(
            "테스트 시험",
            LocalDateTime.now().plusHours(1),
            LocalDateTime.now().minusHours(1)
        );

        assertThatThrownBy(() -> createExamUseCase.execute(1L, request))
            .isInstanceOf(RestApiException.class)
            .satisfies(ex -> assertThat(((RestApiException) ex).getErrorCode().getMessage())
                .contains("만료일은 현재 이후여야 합니다."));

        verify(examRepository, never()).save(any());
        verify(problemRepository, never()).findByStatus(any());
    }

    @Test
    @DisplayName("종료 시각이 현재 이전이면 EXAM009 오류 코드 반환")
    void execute_endsAtNotInFuture_returnsExam009() {
        CreateExamRequest request = new CreateExamRequest(
            "테스트 시험",
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now().minusMinutes(1)
        );

        assertThatThrownBy(() -> createExamUseCase.execute(1L, request))
            .isInstanceOf(RestApiException.class)
            .extracting(ex -> ((RestApiException) ex).getErrorCode().getCode())
            .isEqualTo(ExamErrorStatus.EXAM_ENDS_AT_NOT_IN_FUTURE.getCode().getCode());

        verify(examRepository, never()).save(any());
    }
}
