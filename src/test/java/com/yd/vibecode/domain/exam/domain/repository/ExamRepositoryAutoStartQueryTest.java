package com.yd.vibecode.domain.exam.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.yd.vibecode.domain.exam.domain.entity.Exam;
import com.yd.vibecode.domain.exam.domain.entity.ExamState;
import com.yd.vibecode.global.config.JpaConfig;

@DataJpaTest
@Import(JpaConfig.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.default_schema=",
        "spring.config.import="
})
class ExamRepositoryAutoStartQueryTest {

    @Autowired
    private ExamRepository examRepository;

    @Test
    @DisplayName("WAITING·startsAt<=now 만 자동 시작 대상으로 조회된다")
    void findByStateAndStartsAtLessThanEqual_onlyEligibleWaitingExams() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime past = now.minusMinutes(10);
        LocalDateTime future = now.plusHours(1);

        examRepository.save(exam("past-waiting", ExamState.WAITING, past, now.plusHours(2)));
        examRepository.save(exam("future-waiting", ExamState.WAITING, future, future.plusHours(2)));
        examRepository.save(exam("past-running", ExamState.RUNNING, past, now.plusHours(2)));
        examRepository.save(exam("past-ended", ExamState.ENDED, past, past.plusHours(1)));

        List<Exam> candidates = examRepository.findByStateAndStartsAtLessThanEqual(ExamState.WAITING, now);

        assertThat(candidates).hasSize(1);
        assertThat(candidates.get(0).getTitle()).isEqualTo("past-waiting");
    }

    @Test
    @DisplayName("startsAt이 미래인 WAITING 시험은 조회되지 않는다")
    void findByStateAndStartsAtLessThanEqual_excludesFutureWaiting() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusHours(2);

        examRepository.save(exam("future-only", ExamState.WAITING, future, future.plusHours(3)));

        List<Exam> candidates = examRepository.findByStateAndStartsAtLessThanEqual(ExamState.WAITING, now);

        assertThat(candidates).isEmpty();
    }

    private static Exam exam(String title, ExamState state, LocalDateTime startsAt, LocalDateTime endsAt) {
        return Exam.builder()
                .title(title)
                .state(state)
                .startsAt(startsAt)
                .endsAt(endsAt)
                .version(0)
                .createdBy(1L)
                .build();
    }
}
