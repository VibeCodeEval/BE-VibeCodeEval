package com.yd.vibecode.domain.problem.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yd.vibecode.domain.problem.domain.entity.ProblemSpec;

import java.util.List;
import java.util.Optional;

public interface ProblemSpecRepository extends JpaRepository<ProblemSpec, Long> {

    List<ProblemSpec> findByProblemIdOrderByVersionDesc(Long problemId);
    
    Optional<ProblemSpec> findByProblemIdAndVersion(Long problemId, Integer version);
    
    Optional<ProblemSpec> findBySpecId(Long specId);
}
