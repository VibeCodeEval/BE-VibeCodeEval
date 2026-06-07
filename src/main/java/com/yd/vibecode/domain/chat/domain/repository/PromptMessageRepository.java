package com.yd.vibecode.domain.chat.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yd.vibecode.domain.chat.domain.entity.PromptMessage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PromptMessageRepository extends JpaRepository<PromptMessage, Long> {

    List<PromptMessage> findBySessionIdOrderByTurnAsc(Long sessionId);

    List<PromptMessage> findBySessionId(Long sessionId);

    /**
     * 세션의 최대 turn 값을 조회
     * @param sessionId 세션 ID
     * @return 최대 turn 값 (메시지가 없으면 Optional.empty())
     */
    @Query("SELECT MAX(pm.turn) FROM PromptMessage pm WHERE pm.sessionId = :sessionId")
    Optional<Integer> findMaxTurnBySessionId(@Param("sessionId") Long sessionId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM PromptMessage pm WHERE pm.sessionId IN :sessionIds")
    int deleteBySessionIdIn(@Param("sessionIds") Collection<Long> sessionIds);
}
