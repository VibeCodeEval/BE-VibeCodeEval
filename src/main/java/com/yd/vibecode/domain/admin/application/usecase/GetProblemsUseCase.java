package com.yd.vibecode.domain.admin.application.usecase;

import com.yd.vibecode.domain.admin.application.dto.response.ProblemResponse;
import com.yd.vibecode.domain.problem.domain.repository.ProblemRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetProblemsUseCase {

    private final ProblemRepository problemRepository;

    @Transactional(readOnly = true)
    public List<ProblemResponse> execute() {
        return problemRepository.findAll().stream()
            .map(ProblemResponse::from)
            .toList();
    }
}
