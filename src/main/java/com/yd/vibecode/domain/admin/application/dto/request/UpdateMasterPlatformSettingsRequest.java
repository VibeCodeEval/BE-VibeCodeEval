package com.yd.vibecode.domain.admin.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateMasterPlatformSettingsRequest(
        @NotNull(message = "로그 보관 기간은 필수입니다.")
        @Min(value = 1, message = "로그 보관 기간은 1일 이상이어야 합니다.")
        @Max(value = 3650, message = "로그 보관 기간이 너무 깁니다.")
        @Schema(description = "로그 보관 기간(일)", example = "90")
        Integer logRetentionDays,

        @NotNull(message = "제출물 보관 기간은 필수입니다.")
        @Min(value = 1, message = "제출물 보관 기간은 1일 이상이어야 합니다.")
        @Max(value = 3650, message = "제출물 보관 기간이 너무 깁니다.")
        @Schema(description = "제출물 보관 기간(일)", example = "90")
        Integer submissionRetentionDays,

        @NotNull(message = "만료 데이터 자동 삭제 여부는 필수입니다.")
        @Schema(description = "만료 데이터 자동 삭제 활성화 여부 (false면 향후 삭제 스케줄러 미실행 정책)", example = "true")
        Boolean autoDeleteExpiredData
) {
}
