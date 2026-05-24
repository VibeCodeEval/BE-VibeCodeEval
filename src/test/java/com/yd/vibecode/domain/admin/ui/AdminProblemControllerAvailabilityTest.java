package com.yd.vibecode.domain.admin.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yd.vibecode.domain.admin.application.dto.request.UpdateProblemAvailabilityRequest;
import com.yd.vibecode.domain.admin.application.dto.response.ProblemResponse;
import com.yd.vibecode.domain.admin.application.usecase.CreateProblemUseCase;
import com.yd.vibecode.domain.admin.application.usecase.DeleteProblemUseCase;
import com.yd.vibecode.domain.admin.application.usecase.GetProblemDetailUseCase;
import com.yd.vibecode.domain.admin.application.usecase.GetProblemSpecsUseCase;
import com.yd.vibecode.domain.admin.application.usecase.GetProblemsUseCase;
import com.yd.vibecode.domain.admin.application.usecase.UpdateProblemAvailabilityUseCase;
import com.yd.vibecode.domain.problem.domain.entity.Difficulty;
import com.yd.vibecode.domain.problem.domain.entity.ProblemStatus;
import com.yd.vibecode.global.common.BaseResponse;

@ExtendWith(MockitoExtension.class)
class AdminProblemControllerAvailabilityTest {

    @InjectMocks
    private AdminProblemController adminProblemController;

    @Mock
    private GetProblemsUseCase getProblemsUseCase;

    @Mock
    private CreateProblemUseCase createProblemUseCase;

    @Mock
    private DeleteProblemUseCase deleteProblemUseCase;

    @Mock
    private GetProblemSpecsUseCase getProblemSpecsUseCase;

    @Mock
    private GetProblemDetailUseCase getProblemDetailUseCase;

    @Mock
    private UpdateProblemAvailabilityUseCase updateProblemAvailabilityUseCase;

    @Test
    @DisplayName("문제 사용 가능 여부 변경 — UseCase 결과를 BaseResponse로 반환")
    void updateProblemAvailability_delegatesToUseCase() {
        Long problemId = 1L;
        ProblemResponse response = new ProblemResponse(
                problemId,
                "외판원 순회",
                Difficulty.MEDIUM,
                "[\"dp\"]",
                ProblemStatus.ARCHIVED,
                null
        );
        given(updateProblemAvailabilityUseCase.execute(eq(problemId), any(UpdateProblemAvailabilityRequest.class)))
                .willReturn(response);

        BaseResponse<ProblemResponse> result = adminProblemController.updateProblemAvailability(
                problemId, new UpdateProblemAvailabilityRequest(false));

        assertThat(result.getCode()).isEqualTo("COMMON200");
        assertThat(result.getResult().status()).isEqualTo(ProblemStatus.ARCHIVED);
        verify(updateProblemAvailabilityUseCase).execute(eq(problemId), any(UpdateProblemAvailabilityRequest.class));
    }
}
