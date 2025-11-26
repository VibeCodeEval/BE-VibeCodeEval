package com.yd.vibecode.domain.admin.application.usecase;

import com.yd.vibecode.domain.admin.application.dto.request.CreateEntryCodeRequest;
import com.yd.vibecode.domain.admin.application.dto.response.EntryCodeResponse;
import com.yd.vibecode.domain.admin.domain.service.AdminAuditLogService;
import com.yd.vibecode.domain.auth.domain.entity.EntryCode;
import com.yd.vibecode.domain.auth.domain.repository.EntryCodeRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateEntryCodeUseCase {

    private final EntryCodeRepository entryCodeRepository;
    private final AdminAuditLogService adminAuditLogService;

    @Transactional
    public EntryCodeResponse execute(Long adminId, CreateEntryCodeRequest request) {
        String code = generateUniqueCode();
        
        EntryCode entryCode = EntryCode.builder()
            .code(code)
            .examId(request.examId())
            .problemSetId(request.problemSetId())
            .createdBy(adminId)
            .label(request.label())
            .expiresAt(request.expiresAt())
            .maxUses(request.maxUses())
            .isActive(true)
            .build();

        EntryCode saved = entryCodeRepository.save(entryCode);
        
        // Audit logging
        adminAuditLogService.log(adminId, "CREATE_ENTRY_CODE", Map.of(
            "code", saved.getCode(),
            "label", saved.getLabel() != null ? saved.getLabel() : "",
            "examId", saved.getExamId(),
            "maxUses", saved.getMaxUses()
        ));
        
        return EntryCodeResponse.from(saved);
    }

    private String generateUniqueCode() {
        // Generate random alphanumeric code (8 characters: mix of letters and numbers)
        // Example: "aB3xK9mZ", "7pQ2rN8s"
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789"; // Exclude similar looking chars (0,O,1,I,l)
        StringBuilder code = new StringBuilder();
        java.util.Random random = new java.util.Random();
        
        for (int i = 0; i < 8; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return code.toString();
    }
}
