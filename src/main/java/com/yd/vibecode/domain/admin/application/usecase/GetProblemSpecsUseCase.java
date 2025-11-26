package com.yd.vibecode.domain.admin.application.usecase;

import com.yd.vibecode.domain.admin.application.dto.response.ProblemSpecResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GetProblemSpecsUseCase {

    public List<ProblemSpecResponse> execute(Long problemId) {
        // Mock data for now as Problem domain is empty
        return List.of(
            new ProblemSpecResponse(1L, 1, "Initial release", LocalDateTime.now().minusDays(10)),
            new ProblemSpecResponse(2L, 2, "Fixed typos", LocalDateTime.now().minusDays(5)),
            new ProblemSpecResponse(3L, 3, "Updated constraints", LocalDateTime.now().minusDays(1))
        );
    }
}
