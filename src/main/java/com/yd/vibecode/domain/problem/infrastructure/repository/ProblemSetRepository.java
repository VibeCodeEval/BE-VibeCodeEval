package com.yd.vibecode.domain.problem.infrastructure.repository;

import com.yd.vibecode.domain.problem.infrastructure.entity.ProblemSet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProblemSetRepository extends JpaRepository<ProblemSet, Long> {

    List<ProblemSet> findByCreatedBy(Long createdBy);
}
