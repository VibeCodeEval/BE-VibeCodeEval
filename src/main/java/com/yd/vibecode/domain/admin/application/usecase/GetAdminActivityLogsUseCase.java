package com.yd.vibecode.domain.admin.application.usecase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.admin.application.dto.response.AdminActivityLogPageResponse;
import com.yd.vibecode.domain.admin.application.dto.response.AdminActivityLogResponse;
import com.yd.vibecode.domain.admin.domain.entity.AdminActivityLog;
import com.yd.vibecode.domain.admin.domain.entity.AdminActivityLogType;
import com.yd.vibecode.domain.admin.domain.repository.AdminActivityLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAdminActivityLogsUseCase {

    private static final int MAX_PAGE_SIZE = 100;

    private final AdminActivityLogRepository adminActivityLogRepository;

    public AdminActivityLogPageResponse execute(
            Long adminId,
            String keyword,
            AdminActivityLogType type,
            int page,
            int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        String normalizedKeyword = keyword != null ? keyword.trim() : null;

        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AdminActivityLog> result = adminActivityLogRepository.searchByAdmin(
                adminId,
                type,
                normalizedKeyword,
                pageable);

        return new AdminActivityLogPageResponse(
                result.getContent().stream().map(AdminActivityLogResponse::from).toList(),
                result.getNumber(),
                safeSize,
                result.getTotalElements(),
                result.getTotalPages());
    }
}
