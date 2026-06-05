package com.yd.vibecode.domain.admin.application.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import com.yd.vibecode.domain.admin.domain.entity.MasterActivityLog;

public record MasterActivityLogPageResponse(
        List<MasterActivityLogResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static MasterActivityLogPageResponse from(Page<MasterActivityLog> page) {
        return new MasterActivityLogPageResponse(
                page.getContent().stream().map(MasterActivityLogResponse::from).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
