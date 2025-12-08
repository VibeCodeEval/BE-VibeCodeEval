package com.yd.vibecode.domain.problem.infrastructure.repository;

import com.yd.vibecode.domain.problem.infrastructure.entity.ProblemSetItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProblemSetItemRepository extends JpaRepository<ProblemSetItem, Long> {

    List<ProblemSetItem> findByProblemSetId(Long problemSetId);
    
    List<ProblemSetItem> findByProblemId(Long problemId);
}
