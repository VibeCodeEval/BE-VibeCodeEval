package com.yd.vibecode.domain.chat.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yd.vibecode.domain.chat.domain.entity.PromptMessage;

import java.util.List;

public interface PromptMessageRepository extends JpaRepository<PromptMessage, Long> {

    List<PromptMessage> findBySessionIdOrderByTurnAsc(Long sessionId);

    List<PromptMessage> findBySessionId(Long sessionId);
}
