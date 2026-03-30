package com.yd.vibecode.domain.submission.domain.repository;

import com.yd.vibecode.domain.submission.domain.entity.OutboxEvent;
import com.yd.vibecode.domain.submission.domain.entity.OutboxStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query("SELECT e FROM OutboxEvent e WHERE e.status = :status AND e.nextRetryAt <= :now")
    List<OutboxEvent> findPendingEvents(
            @Param("status") OutboxStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );
}
