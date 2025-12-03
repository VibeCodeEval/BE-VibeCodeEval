package com.yd.vibecode.domain.problem.infra;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProblemSetItemRepository extends JpaRepository<ProblemSetItem, Long> {

    List<ProblemSetItem> findByProblemSetId(Long problemSetId);
    
    List<ProblemSetItem> findByProblemId(Long problemId);
}
