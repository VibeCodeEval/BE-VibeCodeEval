package com.yd.vibecode.domain.chat.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.yd.vibecode.domain.chat.domain.entity.PromptEvaluation;

import com.yd.vibecode.domain.chat.domain.entity.PromptSession;
import java.util.Collection;
import java.util.Optional;

public interface PromptEvaluationRepository extends JpaRepository<PromptEvaluation, Long> {
    Optional<PromptEvaluation> findBySessionAndTurnAndEvaluationType(
        PromptSession session, 
        Integer turn, 
        PromptEvaluation.EvaluationType evaluationType
    );

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM PromptEvaluation pe WHERE pe.session.id IN :sessionIds")
    int deleteBySessionIdIn(@Param("sessionIds") Collection<Long> sessionIds);
}
