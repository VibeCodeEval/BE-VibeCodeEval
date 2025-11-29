package com.yd.vibecode.domain.exam.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yd.vibecode.domain.exam.domain.entity.Exam;

import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    List<Exam> findAll();
    
    List<Exam> findByCreatedBy(Long createdBy);
}
