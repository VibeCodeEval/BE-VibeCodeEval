package com.yd.vibecode.domain.admin.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yd.vibecode.domain.admin.domain.entity.AdminActivityLog;
import com.yd.vibecode.domain.admin.domain.entity.AdminActivityLogType;

public interface AdminActivityLogRepository extends JpaRepository<AdminActivityLog, Long> {

    @Query("""
            SELECT l FROM AdminActivityLog l
            WHERE l.adminId = :adminId
              AND (:type IS NULL OR l.type = :type)
              AND (
                    :keyword IS NULL OR :keyword = ''
                    OR LOWER(l.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(l.message) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  )
            """)
    Page<AdminActivityLog> searchByAdmin(
            @Param("adminId") Long adminId,
            @Param("type") AdminActivityLogType type,
            @Param("keyword") String keyword,
            Pageable pageable);
}
