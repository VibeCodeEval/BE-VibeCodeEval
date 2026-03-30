package com.yd.vibecode.domain.exam.domain.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.exam.domain.entity.Exam;
import com.yd.vibecode.domain.exam.domain.repository.ExamRepository;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.ExamErrorStatus;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ExamService {

    private final ExamRepository examRepository;

    @Transactional(readOnly = true)
    public Exam findById(Long id) {
        return examRepository.findById(id)
                .orElseThrow(() -> new RestApiException(ExamErrorStatus.EXAM_NOT_FOUND));
    }

    public Exam create(Exam exam) {
        return examRepository.save(exam);
    }

    public Exam save(Exam exam) {
        return examRepository.save(exam);
    }

    // 시험 시작: WAITING -> RUNNING, version++
    public Exam startExam(Long examId) {
        Exam exam = findById(examId);
        exam.start();
        return examRepository.save(exam);
    }

    // 시험 종료: RUNNING -> ENDED, version++
    public Exam endExam(Long examId) {
        Exam exam = findById(examId);
        exam.end();
        return examRepository.save(exam);
    }

    // 시험 연장: endsAt += minutes, version++
    public Exam extendExam(Long examId, int minutes) {
        Exam exam = findById(examId);
        exam.extend(minutes);
        return examRepository.save(exam);
    }
}
