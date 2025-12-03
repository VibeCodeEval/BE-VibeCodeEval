package com.yd.vibecode.domain.submission.domain.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.submission.domain.entity.Submission;
import com.yd.vibecode.domain.submission.domain.entity.SubmissionStatus;
import com.yd.vibecode.domain.submission.domain.repository.SubmissionRepository;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.SubmissionErrorStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SubmissionService
 * - 제출 생성 및 Redis Queue enqueue
 * - 코드 해시 계산, LOC 계산
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final RedisTemplate<String, String> redisTemplate;
    
    private static final String SUBMISSION_QUEUE_KEY = "judge:queue";

    @Transactional(readOnly = true)
    public Submission findById(Long id) {
        return submissionRepository.findById(id)
                .orElseThrow(() -> new RestApiException(SubmissionErrorStatus.SUBMISSION_NOT_FOUND));
    }

    /**
     * 제출 생성 및 Redis Queue에 enqueue
     */
    public Submission createAndEnqueue(Long examId, Long participantId, Long specId,
                                      String lang, String code) {
        // 1. 코드 해시 계산
        String codeSha256 = calculateSHA256(code);
        
        // 2. LOC 계산
        int loc = calculateLOC(code);
        
        // 3. 제출 생성
        Submission submission = Submission.builder()
                .examId(examId)
                .participantId(participantId)
                .specId(specId)
                .lang(lang)
                .status(SubmissionStatus.QUEUED)
                .codeInline(code)
                .codeSha256(codeSha256)
                .codeBytes(code.getBytes(StandardCharsets.UTF_8).length)
                .codeLoc(loc)
                .build();
        
        Submission saved = submissionRepository.save(submission);
        
        // 4. Redis Queue에 enqueue
        enqueueSubmission(saved.getId());
        
        log.info("Submission created and enqueued: id={}, examId={}, participantId={}", 
                 saved.getId(), examId, participantId);
        
        return saved;
    }

    /**
     * SHA256 해시 계산
     */
    private String calculateSHA256(String code) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(code.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not found", e);
            return "";
        }
    }

    /**
     * Lines of Code 계산
     */
    private int calculateLOC(String code) {
        if (code == null || code.isEmpty()) {
            return 0;
        }
        String[] lines = code.split("\n");
        int loc = 0;
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("//") && !trimmed.startsWith("#")) {
                loc++;
            }
        }
        return loc;
    }

    /**
     * Redis Queue에 제출 ID enqueue
     */
    private void enqueueSubmission(Long submissionId) {
        try {
            redisTemplate.opsForList().rightPush(SUBMISSION_QUEUE_KEY, submissionId.toString());
            log.info("Submission enqueued to Redis: submissionId={}", submissionId);
        } catch (Exception e) {
            log.error("Failed to enqueue submission to Redis: submissionId={}", submissionId, e);
            throw new RestApiException(SubmissionErrorStatus.QUEUE_FULL);
        }
    }
}
