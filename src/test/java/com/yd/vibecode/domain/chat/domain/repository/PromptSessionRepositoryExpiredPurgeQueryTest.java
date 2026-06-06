package com.yd.vibecode.domain.chat.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.yd.vibecode.domain.chat.domain.entity.PromptSession;
import com.yd.vibecode.domain.exam.domain.entity.Exam;
import com.yd.vibecode.domain.exam.domain.entity.ExamState;
import com.yd.vibecode.domain.exam.domain.repository.ExamRepository;
import com.yd.vibecode.global.config.JpaConfig;

@DataJpaTest
@Import(JpaConfig.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.default_schema=",
        "spring.config.import="
})
class PromptSessionRepositoryExpiredPurgeQueryTest {

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private PromptSessionRepository promptSessionRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("ENDED 시험·cutoff 이전 prompt session id만 조회된다")
    void findIdsByExamStateAndCreatedAtBefore_onlyOldEndedExamSessions() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.minusDays(30);
        LocalDateTime old = cutoff.minusDays(1);
        LocalDateTime recent = cutoff.plusDays(1);

        Exam endedExam = saveExam("ended", ExamState.ENDED);
        Exam runningExam = saveExam("running", ExamState.RUNNING);

        PromptSession oldEnded = saveSession(endedExam.getId(), 1L, old);
        saveSession(endedExam.getId(), 2L, recent);
        saveSession(runningExam.getId(), 3L, old);

        var ids = promptSessionRepository.findIdsByExamStateAndCreatedAtBefore(ExamState.ENDED, cutoff);

        assertThat(ids).containsExactly(oldEnded.getId());
    }

    private Exam saveExam(String title, ExamState state) {
        LocalDateTime now = LocalDateTime.now();
        return examRepository.save(Exam.builder()
                .title(title)
                .state(state)
                .startsAt(now.minusHours(2))
                .endsAt(now.minusHours(1))
                .version(0)
                .createdBy(1L)
                .build());
    }

    private PromptSession saveSession(Long examId, Long participantId, LocalDateTime createdAt) {
        PromptSession session = promptSessionRepository.save(PromptSession.builder()
                .examId(examId)
                .participantId(participantId)
                .specId(1L)
                .build());
        entityManager.getEntityManager().createNativeQuery(
                        "UPDATE prompt_sessions SET created_at = :createdAt WHERE id = :id")
                .setParameter("createdAt", createdAt)
                .setParameter("id", session.getId())
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();
        return promptSessionRepository.findById(session.getId()).orElseThrow();
    }
}
