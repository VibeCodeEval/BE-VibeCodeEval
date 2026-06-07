package com.yd.vibecode.domain.submission.domain.repository;

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

import com.yd.vibecode.domain.exam.domain.entity.Exam;
import com.yd.vibecode.domain.exam.domain.entity.ExamState;
import com.yd.vibecode.domain.exam.domain.repository.ExamRepository;
import com.yd.vibecode.domain.submission.domain.entity.Submission;
import com.yd.vibecode.domain.submission.domain.entity.SubmissionStatus;
import com.yd.vibecode.global.config.JpaConfig;

@DataJpaTest
@Import(JpaConfig.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.default_schema=",
        "spring.config.import="
})
class SubmissionRepositoryExpiredPurgeQueryTest {

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("ENDED 시험·cutoff 이전 제출 id만 조회된다")
    void findIdsByExamStateAndCreatedAtBefore_onlyOldEndedExamSubmissions() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.minusDays(30);
        LocalDateTime old = cutoff.minusDays(1);
        LocalDateTime recent = cutoff.plusDays(1);

        Exam endedExam = saveExam("ended", ExamState.ENDED);
        Exam runningExam = saveExam("running", ExamState.RUNNING);
        Exam waitingExam = saveExam("waiting", ExamState.WAITING);

        Submission oldEnded = saveSubmission(endedExam.getId(), 1L, old);
        saveSubmission(endedExam.getId(), 2L, recent);
        saveSubmission(runningExam.getId(), 3L, old);
        saveSubmission(waitingExam.getId(), 4L, old);

        var ids = submissionRepository.findIdsByExamStateAndCreatedAtBefore(ExamState.ENDED, cutoff);

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

    private Submission saveSubmission(Long examId, Long participantId, LocalDateTime createdAt) {
        Submission submission = submissionRepository.save(Submission.builder()
                .examId(examId)
                .participantId(participantId)
                .specId(1L)
                .lang("python3.11")
                .status(SubmissionStatus.DONE)
                .codeInline("print(1)")
                .build());
        entityManager.getEntityManager().createNativeQuery(
                        "UPDATE submissions SET created_at = :createdAt WHERE id = :id")
                .setParameter("createdAt", createdAt)
                .setParameter("id", submission.getId())
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();
        return submissionRepository.findById(submission.getId()).orElseThrow();
    }
}
