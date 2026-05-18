package com.yd.vibecode.domain.submission.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.entity.AdminRole;
import com.yd.vibecode.domain.auth.domain.service.AdminService;
import com.yd.vibecode.domain.submission.application.dto.response.AdminSubmissionDetailResponse;
import com.yd.vibecode.domain.submission.application.service.SubmissionDetailAssembler;
import com.yd.vibecode.domain.submission.domain.entity.Submission;
import com.yd.vibecode.domain.submission.domain.entity.SubmissionStatus;
import com.yd.vibecode.domain.submission.domain.repository.ScoreRepository;
import com.yd.vibecode.domain.submission.domain.repository.SubmissionRunRepository;
import com.yd.vibecode.domain.submission.domain.service.SubmissionService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;
import com.yd.vibecode.global.exception.code.status.SubmissionErrorStatus;

@ExtendWith(MockitoExtension.class)
class GetAdminSubmissionDetailUseCaseTest {

    @InjectMocks
    private GetAdminSubmissionDetailUseCase getAdminSubmissionDetailUseCase;

    @Mock
    private AdminService adminService;

    @Mock
    private SubmissionService submissionService;

    @Mock
    private SubmissionRunRepository submissionRunRepository;

    @Mock
    private ScoreRepository scoreRepository;

    @Mock
    private SubmissionDetailAssembler submissionDetailAssembler;

    @Test
    @DisplayName("활성 ADMIN 관리자 — 제출 상세 조회 성공")
    void execute_success_asAdmin() {
        Long adminUserId = 1L;
        Long submissionId = 42L;
        Admin admin = Admin.builder()
                .adminNumber("ADM-1")
                .email("a@test.com")
                .passwordHash("hash")
                .role(AdminRole.ADMIN)
                .isActive(true)
                .build();
        Submission submission = Submission.builder()
                .examId(1L)
                .participantId(10L)
                .specId(100L)
                .status(SubmissionStatus.DONE)
                .lang("python")
                .codeInline("print(1)")
                .build();
        ReflectionTestUtils.setField(submission, "id", submissionId);

        given(adminService.findById(adminUserId)).willReturn(admin);
        given(submissionService.findById(submissionId)).willReturn(submission);
        given(submissionRunRepository.findBySubmissionId(submissionId)).willReturn(Collections.emptyList());
        given(scoreRepository.findBySubmissionId(submissionId)).willReturn(Optional.empty());
        given(submissionDetailAssembler.toResponse(submission, Collections.emptyList(), null))
                .willReturn(new com.yd.vibecode.domain.submission.application.dto.response.SubmissionDetailResponse(
                        submissionId,
                        SubmissionStatus.DONE,
                        "python",
                        new com.yd.vibecode.domain.submission.application.dto.response.SubmissionDetailResponse.MetricsInfo(0, 0, 0),
                        new com.yd.vibecode.domain.submission.application.dto.response.SubmissionDetailResponse.TestCaseInfo(0.0, Collections.emptyList()),
                        new com.yd.vibecode.domain.submission.application.dto.response.SubmissionDetailResponse.ScoreInfo(null, null, null, null)));

        AdminSubmissionDetailResponse response =
                getAdminSubmissionDetailUseCase.execute(adminUserId, submissionId);

        assertThat(response.submissionId()).isEqualTo(submissionId);
        assertThat(response.codeInline()).isEqualTo("print(1)");
        verify(submissionService).findById(submissionId);
    }

    @Test
    @DisplayName("비활성 관리자 계정 — 403 (AUTH022)")
    void execute_forbidden_inactiveAdmin() {
        Long adminUserId = 2L;
        Admin inactive = Admin.builder()
                .adminNumber("ADM-2")
                .email("b@test.com")
                .passwordHash("hash")
                .role(AdminRole.ADMIN)
                .isActive(false)
                .build();
        given(adminService.findById(adminUserId)).willReturn(inactive);

        assertThatThrownBy(() -> getAdminSubmissionDetailUseCase.execute(adminUserId, 99L))
                .isInstanceOf(RestApiException.class)
                .satisfies(ex -> assertThat(((RestApiException) ex).getErrorCode().getCode())
                        .isEqualTo(AuthErrorStatus.ADMIN_ACCOUNT_INACTIVE.getCode().getCode()));

        verify(submissionService, never()).findById(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    @DisplayName("존재하지 않는 submissionId — 404 (SUB001)")
    void execute_notFound_submission() {
        Long adminUserId = 1L;
        Long submissionId = 999L;
        Admin admin = Admin.builder()
                .adminNumber("ADM-1")
                .email("a@test.com")
                .passwordHash("hash")
                .role(AdminRole.MASTER)
                .isActive(true)
                .build();
        given(adminService.findById(adminUserId)).willReturn(admin);
        given(submissionService.findById(submissionId))
                .willThrow(new RestApiException(SubmissionErrorStatus.SUBMISSION_NOT_FOUND));

        assertThatThrownBy(() -> getAdminSubmissionDetailUseCase.execute(adminUserId, submissionId))
                .isInstanceOf(RestApiException.class)
                .satisfies(ex -> assertThat(((RestApiException) ex).getErrorCode().getCode())
                        .isEqualTo(SubmissionErrorStatus.SUBMISSION_NOT_FOUND.getCode().getCode()));
    }
}
