package com.yd.vibecode.domain.admin.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.auth.domain.entity.EntryCode;
import com.yd.vibecode.domain.auth.domain.repository.EntryCodeRepository;
import com.yd.vibecode.domain.auth.domain.service.EntryCodeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteEntryCodeUseCase {

    private final EntryCodeService entryCodeService;
    private final EntryCodeRepository entryCodeRepository;

    @Transactional
    public void execute(String code) {
        EntryCode entryCode = entryCodeService.findByCode(code);
        entryCodeRepository.delete(entryCode);
    }
}
