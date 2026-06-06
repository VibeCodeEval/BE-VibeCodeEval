package com.yd.vibecode.domain.exam.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yd.vibecode.domain.exam.domain.entity.Exam;
import com.yd.vibecode.domain.exam.domain.entity.ExamState;

import java.time.LocalDateTime;
import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    List<Exam> findAll();
    
    List<Exam> findByCreatedBy(Long createdBy);

    /**
     * 자동 시작 대상: WAITING 상태이며 시작 시각이 현재 이하인 시험.
     */
    List<Exam> findByStateAndStartsAtLessThanEqual(ExamState state, LocalDateTime startsAtThreshold);

    /**
     * 자동 종료 대상: RUNNING 상태이며 종료 시각이 설정되어 있고 현재 이하인 시험.
     */
    List<Exam> findByStateAndEndsAtIsNotNullAndEndsAtLessThanEqual(ExamState state, LocalDateTime endsAtThreshold);
}
