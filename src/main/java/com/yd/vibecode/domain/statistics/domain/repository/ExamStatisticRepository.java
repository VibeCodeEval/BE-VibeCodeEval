package com.yd.vibecode.domain.statistics.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yd.vibecode.domain.statistics.domain.entity.ExamStatistic;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ExamStatisticRepository extends JpaRepository<ExamStatistic, Long> {

    @Query("SELECT e FROM ExamStatistic e WHERE e.examId = :examId ORDER BY e.bucketStart DESC LIMIT 1")
    Optional<ExamStatistic> findLatestByExamId(@Param("examId") Long examId);

    List<ExamStatistic> findByExamIdAndBucketStartBetweenOrderByBucketStartAsc(
            Long examId, LocalDateTime from, LocalDateTime to);

    List<ExamStatistic> findByExamIdOrderByBucketStartDesc(Long examId);
}
