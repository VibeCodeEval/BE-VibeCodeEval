package com.yd.vibecode.domain.admin.application.usecase;

import com.yd.vibecode.domain.admin.application.dto.request.UpdateEntryCodeRequest;
import com.yd.vibecode.domain.admin.application.dto.response.EntryCodeResponse;
import com.yd.vibecode.domain.auth.domain.entity.EntryCode;
import com.yd.vibecode.domain.auth.domain.repository.EntryCodeRepository;
import com.yd.vibecode.domain.auth.domain.service.EntryCodeService;
import com.yd.vibecode.global.util.CodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateEntryCodeUseCase {

    private final EntryCodeService entryCodeService;
    private final EntryCodeRepository entryCodeRepository;

    @Transactional
    public EntryCodeResponse execute(String code, UpdateEntryCodeRequest request) {
        // 1. 기존 코드 조회 및 비활성화
        EntryCode oldEntryCode = entryCodeService.findByCode(code);
        oldEntryCode.deactivate();

        // 2. 새로운 코드 생성 (재발급)
        String newCode = CodeGenerator.generate();
        EntryCode newEntryCode = EntryCode.builder()
            .code(newCode)
            .examId(oldEntryCode.getExamId())
            .problemSetId(oldEntryCode.getProblemSetId())
            .createdBy(oldEntryCode.getCreatedBy())
            .label(oldEntryCode.getLabel())
            .expiresAt(oldEntryCode.getExpiresAt())
            .maxUses(oldEntryCode.getMaxUses())
            .isActive(true)
            .build();

        entryCodeRepository.save(newEntryCode);

        return EntryCodeResponse.from(newEntryCode);
    }
}
