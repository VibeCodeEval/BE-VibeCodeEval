package com.yd.vibecode.domain.auth.domain.service;

import org.springframework.stereotype.Service;

import com.yd.vibecode.domain.auth.domain.entity.EntryCode;
import com.yd.vibecode.domain.auth.domain.repository.EntryCodeRepository;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EntryCodeService {

    private final EntryCodeRepository entryCodeRepository;

    public EntryCode findByCode(String code) {
        return entryCodeRepository.findByCode(code)
                .orElseThrow(() -> new RestApiException(AuthErrorStatus.INVALID_CODE));
    }

    public void validateEntryCode(EntryCode entryCode) {
        if (!entryCode.isValid()) {
            if (entryCode.isExpired()) {
                throw new RestApiException(AuthErrorStatus.CODE_EXPIRED);
            }
            if (entryCode.isMaxUsesReached()) {
                throw new RestApiException(AuthErrorStatus.CODE_CAP_REACHED);
            }
            throw new RestApiException(AuthErrorStatus.INVALID_CODE);
        }
    }

    public void incrementUsedCount(EntryCode entryCode) {
        entryCode.incrementUsedCount();
    }
}

