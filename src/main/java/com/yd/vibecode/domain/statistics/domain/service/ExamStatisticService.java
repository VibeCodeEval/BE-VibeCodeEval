package com.yd.vibecode.domain.statistics.domain.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.statistics.domain.entity.ExamStatistic;
import com.yd.vibecode.domain.statistics.domain.repository.ExamStatisticRepository;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamStatisticService {

    private final ExamStatisticRepository examStatisticRepository;

    @Transactional
    public ExamStatistic save(ExamStatistic statistic) {
        return examStatisticRepository.save(statistic);
    }

    @Transactional(readOnly = true)
    public ExamStatistic findLatestByExamId(Long examId) {
        return examStatisticRepository.findLatestByExamId(examId)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<ExamStatistic> findByExamIdAndDateRange(Long examId, LocalDateTime from, LocalDateTime to) {
        return examStatisticRepository.findByExamIdAndBucketStartBetweenOrderByBucketStartAsc(examId, from, to);
    }

    @Transactional(readOnly = true)
    public List<ExamStatistic> findByExamId(Long examId) {
        return examStatisticRepository.findByExamIdOrderByBucketStartDesc(examId);
    }
}
