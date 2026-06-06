package com.yd.vibecode.domain.admin.domain.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yd.vibecode.domain.admin.domain.entity.MasterActivityLog;
import com.yd.vibecode.domain.admin.domain.entity.MasterActivityLogType;

public interface MasterActivityLogRepository extends JpaRepository<MasterActivityLog, Long> {

    @Query("""
            SELECT l FROM MasterActivityLog l
            WHERE (:type IS NULL OR l.type = :type)
              AND (
                    :keyword IS NULL OR :keyword = ''
                    OR LOWER(l.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(l.message) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  )
            """)
    Page<MasterActivityLog> search(
            @Param("type") MasterActivityLogType type,
            @Param("keyword") String keyword,
            Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM MasterActivityLog l WHERE l.createdAt < :cutoff")
    int deleteByCreatedAtBefore(@Param("cutoff") LocalDateTime cutoff);
}
