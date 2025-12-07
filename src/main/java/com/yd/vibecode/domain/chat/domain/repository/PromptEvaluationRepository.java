package com.yd.vibecode.domain.chat.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.yd.vibecode.domain.chat.domain.entity.PromptEvaluation;

import com.yd.vibecode.domain.chat.domain.entity.PromptSession;
import java.util.Optional;

public interface PromptEvaluationRepository extends JpaRepository<PromptEvaluation, Long> {
    Optional<PromptEvaluation> findBySessionAndTurnAndEvaluationType(
        PromptSession session, 
        Integer turn, 
        PromptEvaluation.EvaluationType evaluationType
    );
}
