package com.yd.vibecode.domain.admin.application.usecase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.admin.application.dto.response.MasterActivityLogPageResponse;
import com.yd.vibecode.domain.admin.application.dto.response.MasterActivityLogResponse;
import com.yd.vibecode.domain.admin.domain.entity.MasterActivityLog;
import com.yd.vibecode.domain.admin.domain.entity.MasterActivityLogType;
import com.yd.vibecode.domain.admin.domain.repository.MasterActivityLogRepository;
import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.service.AdminService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMasterActivityLogsUseCase {

    private static final int MAX_PAGE_SIZE = 100;

    private final AdminService adminService;
    private final MasterActivityLogRepository masterActivityLogRepository;

    public MasterActivityLogPageResponse execute(
            Long requesterId,
            String keyword,
            MasterActivityLogType type,
            int page,
            int size) {
        Admin requester = adminService.findById(requesterId);
        if (!requester.isMaster()) {
            throw new RestApiException(AuthErrorStatus.MASTER_ONLY);
        }

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        String normalizedKeyword = keyword != null ? keyword.trim() : null;

        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<MasterActivityLog> result = masterActivityLogRepository.search(
                type,
                normalizedKeyword,
                pageable);

        return new MasterActivityLogPageResponse(
                result.getContent().stream().map(MasterActivityLogResponse::from).toList(),
                result.getNumber(),
                safeSize,
                result.getTotalElements(),
                result.getTotalPages());
    }
}
