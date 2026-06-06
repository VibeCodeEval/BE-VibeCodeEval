package com.yd.vibecode.domain.submission.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yd.vibecode.domain.submission.domain.entity.SubmissionRun;

import java.util.Collection;
import java.util.List;

public interface SubmissionRunRepository extends JpaRepository<SubmissionRun, Long> {

    List<SubmissionRun> findBySubmissionId(Long submissionId);
    
    List<SubmissionRun> findBySubmissionIdOrderByCaseIndexAsc(Long submissionId);

    void deleteBySubmissionId(Long submissionId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM SubmissionRun r WHERE r.submissionId IN :submissionIds")
    int deleteBySubmissionIdIn(@Param("submissionIds") Collection<Long> submissionIds);
}
