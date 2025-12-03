package com.yd.vibecode.domain.submission.domain.service;

import com.yd.vibecode.domain.submission.domain.entity.Submission;
import com.yd.vibecode.domain.submission.domain.entity.SubmissionStatus;
import com.yd.vibecode.domain.submission.domain.repository.SubmissionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @InjectMocks
    private SubmissionService submissionService;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ListOperations<String, String> listOperations;

    @Test
    @DisplayName("제출 생성 및 Enqueue 성공: 해시 및 LOC 계산 확인")
    void createAndEnqueue_Success() {
        // given
        Long examId = 1L;
        Long participantId = 100L;
        Long specId = 10L;
        String lang = "python3.11";
        String code = """
            # This is a comment
            def solve():
                print("hello")
            
            solve()
            """;
        
        // Mock Redis
        given(redisTemplate.opsForList()).willReturn(listOperations);
        
        // Mock Repository
        given(submissionRepository.save(any(Submission.class))).willAnswer(invocation -> {
            Submission s = invocation.getArgument(0);
            // Reflection to set ID
            java.lang.reflect.Field idField = s.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(s, 123L);
            return s;
        });

        // when
        Submission result = submissionService.createAndEnqueue(examId, participantId, specId, lang, code);

        // then
        assertThat(result.getId()).isEqualTo(123L);
        assertThat(result.getStatus()).isEqualTo(SubmissionStatus.QUEUED);
        assertThat(result.getCodeLoc()).isEqualTo(3); // 3 lines of code (excluding comment and empty line)
        assertThat(result.getCodeSha256()).isNotNull();
        
        verify(listOperations).rightPush(eq("judge:queue"), eq("123"));
    }
}
