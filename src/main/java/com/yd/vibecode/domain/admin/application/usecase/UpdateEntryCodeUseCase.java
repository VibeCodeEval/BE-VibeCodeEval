package com.yd.vibecode.domain.admin.application.usecase;

import com.yd.vibecode.domain.admin.application.dto.request.UpdateEntryCodeRequest;
import com.yd.vibecode.domain.admin.application.dto.response.EntryCodeResponse;
import com.yd.vibecode.domain.auth.domain.entity.EntryCode;
import com.yd.vibecode.domain.auth.domain.service.EntryCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateEntryCodeUseCase {

    private final EntryCodeService entryCodeService;

    @Transactional
    public EntryCodeResponse execute(String code, UpdateEntryCodeRequest request) {
        EntryCode entryCode = entryCodeService.findByCode(code);

        // request.isActive() 값에 따라 활성/비활성 처리 (코드 재발급 없음)
        entryCode.update(request.isActive());

        return EntryCodeResponse.from(entryCode);
    }
}
