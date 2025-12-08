package com.yd.vibecode.domain.admin.application.usecase;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.admin.application.dto.response.EntryCodeResponse;
import com.yd.vibecode.domain.auth.domain.entity.EntryCode;
import com.yd.vibecode.domain.auth.domain.repository.EntryCodeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetEntryCodesUseCase {

    private final EntryCodeRepository entryCodeRepository;

    public List<EntryCodeResponse> execute(Long examId, Boolean isActive) {
        List<EntryCode> entryCodes;

        if (isActive != null) {
            entryCodes = entryCodeRepository.findByExamIdAndIsActive(examId, isActive);
        } else {
            entryCodes = entryCodeRepository.findByExamId(examId);
        }

        return entryCodes.stream()
            .map(EntryCodeResponse::from)
            .collect(Collectors.toList());
    }
}
