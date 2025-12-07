package com.yd.vibecode.domain.problem.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yd.vibecode.domain.problem.domain.entity.Difficulty;
import com.yd.vibecode.domain.problem.domain.entity.Problem;
import com.yd.vibecode.domain.problem.domain.entity.ProblemSpec;
import com.yd.vibecode.domain.problem.domain.entity.ProblemStatus;
import com.yd.vibecode.domain.problem.domain.repository.ProblemRepository;
import com.yd.vibecode.domain.problem.domain.repository.ProblemSpecRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProblemInitializer implements ApplicationRunner {

    private final ProblemRepository problemRepository;
    private final ProblemSpecRepository problemSpecRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        initializeProblem("problems/problem1.json");
        initializeProblem("problems/problem2.json");
    }

    private void initializeProblem(String resourcePath) {
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);
            ProblemDefinition definition = objectMapper.readValue(resource.getInputStream(), ProblemDefinition.class);

            if (problemRepository.findByTitle(definition.getTitle()).isPresent()) {
                return;
            }

            log.info("Initializing problem: {}", definition.getTitle());

            Problem problem = Problem.builder()
                    .title(definition.getTitle())
                    .difficulty(Difficulty.valueOf(definition.getDifficulty()))
                    .tags(objectMapper.writeValueAsString(definition.getTags()))
                    .status(ProblemStatus.valueOf(definition.getStatus()))
                    .build();

            problemRepository.save(problem);

            ProblemSpec spec = ProblemSpec.builder()
                    .problemId(problem.getId())
                    .version(1)
                    .contentMd(String.join("\n", definition.getContentMd()))
                    .checkerJson(objectMapper.writeValueAsString(definition.getCheckerJson()))
                    .rubricJson(objectMapper.writeValueAsString(definition.getRubricJson()))
                    .changelogMd(definition.getChangelogMd())
                    .publishedAt(LocalDateTime.now())
                    .build();

            problemSpecRepository.save(spec);
            problem.updateCurrentSpecId(spec.getSpecId());

        } catch (IOException e) {
            log.error("Failed to initialize problem from resource: {}", resourcePath, e);
        }
    }

    @Getter
    @Setter
    private static class ProblemDefinition {
        private String title;
        private String difficulty;
        private List<String> tags;
        private String status;
        private List<String> contentMd;
        private Object checkerJson;
        private Object rubricJson;
        private String changelogMd;
    }
}
