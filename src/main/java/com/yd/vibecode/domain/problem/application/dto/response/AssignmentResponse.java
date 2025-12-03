package com.yd.vibecode.domain.problem.application.dto.response;

import com.yd.vibecode.domain.problem.domain.entity.Difficulty;

import java.util.List;

public record AssignmentResponse(
    ProblemInfo problem,
    SpecInfo spec
) {
    public record ProblemInfo(
        Long id,
        String title,
        String contentMd,
        List<String> tags,
        Difficulty difficulty
    ) {
    }

    public record SpecInfo(
        Integer version,
        LimitsInfo limits,
        RestrictionsInfo restrictions,
        CheckerInfo checker
    ) {
    }

    public record LimitsInfo(
        Integer timeMs,
        Integer memoryMb
    ) {
    }

    public record RestrictionsInfo(
        List<String> allowedLangs,
        List<String> forbiddenApis
    ) {
    }

    public record CheckerInfo(
        String type
    ) {
    }
}
