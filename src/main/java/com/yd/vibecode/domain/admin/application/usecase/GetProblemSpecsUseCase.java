package com.yd.vibecode.domain.admin.application.usecase;

import com.yd.vibecode.domain.admin.application.dto.response.ProblemSpecResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GetProblemSpecsUseCase {

    public List<ProblemSpecResponse> execute(Long problemId) {
        // Problem 도메인이 비어있으므로 현재는 Mock 데이터 사용
        return List.of(
            new ProblemSpecResponse(1L, 1, "Initial release", LocalDateTime.now().minusDays(10)),
            new ProblemSpecResponse(2L, 2, "Fixed typos", LocalDateTime.now().minusDays(5)),
            new ProblemSpecResponse(3L, 3, "Updated constraints", LocalDateTime.now().minusDays(1))
        );
    }
}
