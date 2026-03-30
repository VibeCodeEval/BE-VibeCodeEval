package com.yd.vibecode.domain.submission.domain.repository;

import com.yd.vibecode.domain.submission.domain.entity.OutboxEvent;
import com.yd.vibecode.domain.submission.domain.entity.OutboxStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    @Query("SELECT e FROM OutboxEvent e WHERE e.status = :status AND e.nextRetryAt <= :now")
    List<OutboxEvent> findPendingEvents(
            @Param("status") OutboxStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    /**
     * 2분 이상 PROCESSING 상태로 고착된 이벤트 조회 (서버 크래시 복구용)
     * 이 이벤트들은 PENDING으로 초기화하여 Poller가 재처리할 수 있도록 함
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    @Query("SELECT e FROM OutboxEvent e WHERE e.status = :status AND e.updatedAt <= :staleThreshold")
    List<OutboxEvent> findStaleProcessingEvents(
            @Param("status") OutboxStatus status,
            @Param("staleThreshold") LocalDateTime staleThreshold
    );
}
