package com.yd.vibecode.domain.admin.domain.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.admin.domain.entity.MasterActivityLog;
import com.yd.vibecode.domain.admin.domain.entity.MasterActivityLogType;
import com.yd.vibecode.domain.admin.domain.repository.MasterActivityLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MasterActivityLogService {

    private static final String SIGNUP_CODE_ISSUED_TITLE = "관리자 가입 번호가 발급되었습니다";
    private static final String SIGNUP_CODE_ISSUED_MESSAGE = "새 관리자가 가입할 수 있는 가입 번호가 발급되었습니다.";

    private static final String SIGNUP_CODE_DEACTIVATED_TITLE = "관리자 가입 번호가 비활성화되었습니다";
    private static final String SIGNUP_CODE_DEACTIVATED_MESSAGE = "관리자 가입 번호가 비활성화되었습니다.";

    private static final String SIGNUP_CODE_REACTIVATED_TITLE = "관리자 가입 번호가 재활성화되었습니다";
    private static final String SIGNUP_CODE_REACTIVATED_MESSAGE = "관리자 가입 번호가 다시 사용 가능 상태로 변경되었습니다.";

    private static final String ADMIN_ACCOUNT_DEACTIVATED_TITLE = "관리자 계정이 비활성화되었습니다";
    private static final String ADMIN_ACCOUNT_REACTIVATED_TITLE = "관리자 계정이 재활성화되었습니다";

    private static final String ADMIN_SIGNED_UP_TITLE = "관리자 가입이 완료되었습니다";
    private static final String ADMIN_ACCOUNT_DELETED_TITLE = "관리자 계정이 삭제되었습니다";
    private static final String ADMIN_PASSWORD_RESET_TITLE = "관리자 비밀번호가 재설정되었습니다";

    private static final String PLATFORM_SETTINGS_UPDATED_TITLE = "플랫폼 전역 설정이 변경되었습니다";
    private static final String PLATFORM_SETTINGS_UPDATED_MESSAGE = "플랫폼 전역 설정이 변경되었습니다.";

    private final MasterActivityLogRepository masterActivityLogRepository;

    @Transactional
    public MasterActivityLog logSignupCodeIssued(Long masterId) {
        return save(masterId, null, MasterActivityLogType.ADMIN_SIGNUP_CODE_ISSUED,
                SIGNUP_CODE_ISSUED_TITLE, SIGNUP_CODE_ISSUED_MESSAGE);
    }

    @Transactional
    public MasterActivityLog logSignupCodeDeactivated(Long masterId, Long targetAdminId, String displayName) {
        if (targetAdminId != null) {
            String label = resolveTargetDisplayName(displayName, targetAdminId);
            return save(masterId, targetAdminId, MasterActivityLogType.ADMIN_SIGNUP_CODE_DEACTIVATED,
                    ADMIN_ACCOUNT_DEACTIVATED_TITLE,
                    String.format("'%s' 관리자 계정이 비활성화되었습니다.", label));
        }
        return save(masterId, null, MasterActivityLogType.ADMIN_SIGNUP_CODE_DEACTIVATED,
                SIGNUP_CODE_DEACTIVATED_TITLE, SIGNUP_CODE_DEACTIVATED_MESSAGE);
    }

    @Transactional
    public MasterActivityLog logSignupCodeReactivated(Long masterId, Long targetAdminId, String displayName) {
        if (targetAdminId != null) {
            String label = resolveTargetDisplayName(displayName, targetAdminId);
            return save(masterId, targetAdminId, MasterActivityLogType.ADMIN_SIGNUP_CODE_REACTIVATED,
                    ADMIN_ACCOUNT_REACTIVATED_TITLE,
                    String.format("'%s' 관리자 계정이 재활성화되었습니다.", label));
        }
        return save(masterId, null, MasterActivityLogType.ADMIN_SIGNUP_CODE_REACTIVATED,
                SIGNUP_CODE_REACTIVATED_TITLE, SIGNUP_CODE_REACTIVATED_MESSAGE);
    }

    @Transactional
    public MasterActivityLog logAdminSignedUp(Long masterId, Long targetAdminId, String displayName) {
        String label = resolveTargetDisplayName(displayName, targetAdminId);
        return save(masterId, targetAdminId, MasterActivityLogType.ADMIN_SIGNED_UP,
                ADMIN_SIGNED_UP_TITLE,
                String.format("'%s' 관리자가 가입을 완료했습니다.", label));
    }

    @Transactional
    public MasterActivityLog logAdminAccountDeleted(Long masterId, Long targetAdminId, String displayName) {
        String label = resolveTargetDisplayName(displayName, targetAdminId);
        return save(masterId, targetAdminId, MasterActivityLogType.ADMIN_ACCOUNT_DELETED,
                ADMIN_ACCOUNT_DELETED_TITLE,
                String.format("'%s' 관리자 계정이 삭제되었습니다.", label));
    }

    @Transactional
    public MasterActivityLog logPlatformSettingsUpdated(Long masterId) {
        return save(masterId, null, MasterActivityLogType.PLATFORM_SETTINGS_UPDATED,
                PLATFORM_SETTINGS_UPDATED_TITLE, PLATFORM_SETTINGS_UPDATED_MESSAGE);
    }

    @Transactional
    public MasterActivityLog logAdminPasswordReset(Long masterId, Long targetAdminId, String displayName) {
        String label = resolveTargetDisplayName(displayName, targetAdminId);
        return save(masterId, targetAdminId, MasterActivityLogType.ADMIN_PASSWORD_RESET,
                ADMIN_PASSWORD_RESET_TITLE,
                String.format("'%s' 관리자 비밀번호가 재설정되었습니다.", label));
    }

    static String resolveTargetDisplayName(String displayName, Long adminId) {
        if (displayName != null && !displayName.isBlank()) {
            return displayName.trim();
        }
        return "관리자 ID " + adminId;
    }

    private MasterActivityLog save(
            Long masterId,
            Long targetAdminId,
            MasterActivityLogType type,
            String title,
            String message) {
        MasterActivityLog log = MasterActivityLog.builder()
                .masterId(masterId)
                .targetAdminId(targetAdminId)
                .type(type)
                .title(title)
                .message(message)
                .build();
        return masterActivityLogRepository.save(log);
    }
}
