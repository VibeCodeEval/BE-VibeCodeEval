package com.yd.vibecode.domain.problem.infra;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProblemSetRepository extends JpaRepository<ProblemSet, Long> {

    List<ProblemSet> findByCreatedBy(Long createdBy);
}
