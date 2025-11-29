package com.yd.vibecode.domain.problem.application.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.exam.domain.service.ExamParticipantService;
import com.yd.vibecode.domain.problem.application.dto.response.AssignmentResponse;
import com.yd.vibecode.domain.problem.domain.entity.Difficulty;
import com.yd.vibecode.domain.problem.domain.entity.Problem;
import com.yd.vibecode.domain.problem.domain.entity.ProblemSpec;
import com.yd.vibecode.domain.problem.domain.service.ProblemService;
import com.yd.vibecode.domain.problem.domain.service.ProblemSpecService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.ProblemErrorStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetAssignmentUseCaseTest {

    @InjectMocks
    private GetAssignmentUseCase getAssignmentUseCase;

    @Mock
    private ExamParticipantService examParticipantService;

    @Mock
    private ProblemService problemService;

    @Mock
    private ProblemSpecService problemSpecService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("배정 문제 조회 성공: JSON 파싱 및 기본값 확인")
    void execute_Success() {
        // given
        Long examId = 1L;
        Long userId = 100L;
        Long problemId = 10L;
        Long specId = 20L;

        ExamParticipant examParticipant = ExamParticipant.builder()
                .examId(examId)
                .participantId(userId)
                .assignedProblemId(problemId)
                .specId(specId)
                .build();

        Problem problem = Problem.builder()
                .title("Test Problem")
                .difficulty(Difficulty.MEDIUM)
                .tags("[\"dp\", \"math\"]")
                .build();

        String checkerJson = """
            {
                "type": "equality",
                "limits": {
                    "timeMs": 3000,
                    "memoryMb": 256
                },
                "restrictions": {
                    "allowedLangs": ["java", "python"],
                    "forbiddenApis": ["System.exit"]
                }
            }
            """;

        ProblemSpec spec = ProblemSpec.builder()
                .problemId(problemId)
                .version(1)
                .contentMd("# Problem Content")
                .checkerJson(checkerJson)
                .build();

        given(examParticipantService.findByExamIdAndParticipantId(examId, userId)).willReturn(examParticipant);
        given(problemService.findById(problemId)).willReturn(problem);
        given(problemSpecService.findBySpecId(specId)).willReturn(spec);

        // when
        AssignmentResponse response = getAssignmentUseCase.execute(examId, userId);

        // then
        assertThat(response.problem().title()).isEqualTo("Test Problem");
        assertThat(response.problem().tags()).containsExactly("dp", "math");
        assertThat(response.problem().difficulty()).isEqualTo(Difficulty.MEDIUM);
        
        assertThat(response.spec().limits().timeMs()).isEqualTo(3000);
        assertThat(response.spec().limits().memoryMb()).isEqualTo(256);
        assertThat(response.spec().restrictions().allowedLangs()).containsExactly("java", "python");
        assertThat(response.spec().restrictions().forbiddenApis()).containsExactly("System.exit");
    }

    @Test
    @DisplayName("배정 문제 조회 실패: 배정된 문제가 없는 경우")
    void execute_Fail_NoAssignment() {
        // given
        Long examId = 1L;
        Long userId = 100L;
        
        // assignedProblemId is null
        ExamParticipant examParticipant = ExamParticipant.builder()
                .examId(examId)
                .participantId(userId)
                .build();

        given(examParticipantService.findByExamIdAndParticipantId(examId, userId)).willReturn(examParticipant);

        // when & then
        assertThatThrownBy(() -> getAssignmentUseCase.execute(examId, userId))
                .isInstanceOf(RestApiException.class)
                .extracting("errorCode.code").isEqualTo(ProblemErrorStatus.NO_ASSIGNED_PROBLEM.getCode().getCode());
    }
}
