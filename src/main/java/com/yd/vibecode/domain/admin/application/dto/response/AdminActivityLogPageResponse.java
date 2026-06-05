package com.yd.vibecode.domain.admin.application.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import com.yd.vibecode.domain.admin.domain.entity.AdminActivityLog;

public record AdminActivityLogPageResponse(
        List<AdminActivityLogResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static AdminActivityLogPageResponse from(Page<AdminActivityLog> page) {
        return new AdminActivityLogPageResponse(
                page.getContent().stream().map(AdminActivityLogResponse::from).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
