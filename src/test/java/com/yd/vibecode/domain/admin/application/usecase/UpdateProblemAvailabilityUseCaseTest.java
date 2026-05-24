package com.yd.vibecode.domain.admin.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yd.vibecode.domain.admin.application.dto.request.UpdateProblemAvailabilityRequest;
import com.yd.vibecode.domain.problem.domain.entity.Difficulty;
import com.yd.vibecode.domain.problem.domain.entity.Problem;
import com.yd.vibecode.domain.problem.domain.entity.ProblemStatus;
import com.yd.vibecode.domain.problem.domain.repository.ProblemRepository;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.ProblemErrorStatus;

@ExtendWith(MockitoExtension.class)
class UpdateProblemAvailabilityUseCaseTest {

    @InjectMocks
    private UpdateProblemAvailabilityUseCase updateProblemAvailabilityUseCase;

    @Mock
    private ProblemRepository problemRepository;

    @Test
    @DisplayName("PUBLISHED 문제를 available=false로 변경하면 ARCHIVED가 된다")
    void execute_publishToArchived_success() {
        Problem problem = publishedProblem(1L);
        given(problemRepository.findById(1L)).willReturn(Optional.of(problem));
        given(problemRepository.countByStatus(ProblemStatus.PUBLISHED)).willReturn(2L);

        var response = updateProblemAvailabilityUseCase.execute(
                1L, new UpdateProblemAvailabilityRequest(false));

        assertThat(response.status()).isEqualTo(ProblemStatus.ARCHIVED);
        assertThat(problem.getStatus()).isEqualTo(ProblemStatus.ARCHIVED);
    }

    @Test
    @DisplayName("ARCHIVED 문제를 available=true로 변경하면 PUBLISHED가 된다")
    void execute_archivedToPublished_success() {
        Problem problem = Problem.builder()
                .title("문제 B")
                .difficulty(Difficulty.EASY)
                .status(ProblemStatus.ARCHIVED)
                .build();
        given(problemRepository.findById(2L)).willReturn(Optional.of(problem));

        var response = updateProblemAvailabilityUseCase.execute(
                2L, new UpdateProblemAvailabilityRequest(true));

        assertThat(response.status()).isEqualTo(ProblemStatus.PUBLISHED);
        assertThat(problem.getStatus()).isEqualTo(ProblemStatus.PUBLISHED);
        verify(problemRepository, never()).countByStatus(ProblemStatus.PUBLISHED);
    }

    @Test
    @DisplayName("마지막 PUBLISHED 문제를 available=false로 변경하려 하면 400")
    void execute_lastPublishedCannotBeArchived() {
        Problem problem = publishedProblem(3L);
        given(problemRepository.findById(3L)).willReturn(Optional.of(problem));
        given(problemRepository.countByStatus(ProblemStatus.PUBLISHED)).willReturn(1L);

        assertThatThrownBy(() -> updateProblemAvailabilityUseCase.execute(
                3L, new UpdateProblemAvailabilityRequest(false)))
                .isInstanceOf(RestApiException.class)
                .satisfies(ex -> assertThat(((RestApiException) ex).getErrorCode().getMessage())
                        .isEqualTo("최소 1개 이상의 문제는 사용 가능해야 합니다."));

        assertThat(problem.getStatus()).isEqualTo(ProblemStatus.PUBLISHED);
    }

    @Test
    @DisplayName("존재하지 않는 problemId는 PROBLEM_NOT_FOUND")
    void execute_notFound() {
        given(problemRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> updateProblemAvailabilityUseCase.execute(
                999L, new UpdateProblemAvailabilityRequest(false)))
                .isInstanceOf(RestApiException.class)
                .extracting(ex -> ((RestApiException) ex).getErrorCode().getCode())
                .isEqualTo(ProblemErrorStatus.PROBLEM_NOT_FOUND.getCode().getCode());
    }

    @Test
    @DisplayName("이미 PUBLISHED인 문제에 available=true는 no-op")
    void execute_alreadyPublished_noOp() {
        Problem problem = publishedProblem(4L);
        given(problemRepository.findById(4L)).willReturn(Optional.of(problem));

        var response = updateProblemAvailabilityUseCase.execute(
                4L, new UpdateProblemAvailabilityRequest(true));

        assertThat(response.status()).isEqualTo(ProblemStatus.PUBLISHED);
        verify(problemRepository, never()).countByStatus(ProblemStatus.PUBLISHED);
    }

    @Test
    @DisplayName("이미 ARCHIVED인 문제에 available=false는 no-op")
    void execute_alreadyArchived_noOp() {
        Problem problem = Problem.builder()
                .title("문제 C")
                .difficulty(Difficulty.MEDIUM)
                .status(ProblemStatus.ARCHIVED)
                .build();
        given(problemRepository.findById(5L)).willReturn(Optional.of(problem));

        var response = updateProblemAvailabilityUseCase.execute(
                5L, new UpdateProblemAvailabilityRequest(false));

        assertThat(response.status()).isEqualTo(ProblemStatus.ARCHIVED);
        verify(problemRepository, never()).countByStatus(ProblemStatus.PUBLISHED);
    }

    private static Problem publishedProblem(Long id) {
        Problem problem = Problem.builder()
                .title("문제 A")
                .difficulty(Difficulty.EASY)
                .status(ProblemStatus.PUBLISHED)
                .build();
        org.springframework.test.util.ReflectionTestUtils.setField(problem, "id", id);
        return problem;
    }
}
