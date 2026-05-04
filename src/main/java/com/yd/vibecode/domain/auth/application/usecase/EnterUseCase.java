package com.yd.vibecode.domain.auth.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.auth.application.dto.request.EnterRequest;
import com.yd.vibecode.domain.auth.application.dto.response.EnterResponse;
import com.yd.vibecode.domain.auth.domain.entity.EntryCode;
import com.yd.vibecode.domain.auth.domain.entity.User;
import com.yd.vibecode.domain.auth.domain.service.EntryCodeService;
import com.yd.vibecode.domain.auth.domain.service.UserService;
import com.yd.vibecode.domain.exam.application.dto.response.ExamInfoResponse;
import com.yd.vibecode.domain.exam.application.dto.response.SessionInfoResponse;
import com.yd.vibecode.domain.exam.domain.entity.Exam;
import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.exam.domain.repository.ExamParticipantRepository;
import com.yd.vibecode.domain.exam.domain.service.ExamParticipantService;
import com.yd.vibecode.domain.exam.domain.service.ExamService;
import com.yd.vibecode.domain.problem.domain.entity.Problem;
import com.yd.vibecode.domain.problem.domain.service.ProblemService;
import com.yd.vibecode.domain.problem.infrastructure.entity.ProblemSetItem;
import com.yd.vibecode.domain.problem.infrastructure.repository.ProblemSetItemRepository;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.ProblemErrorStatus;
import com.yd.vibecode.global.security.TokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EnterUseCase {

    private final EntryCodeService entryCodeService;
    private final UserService userService;
    private final ExamParticipantService examParticipantService;
    private final ExamParticipantRepository examParticipantRepository;
    private final TokenProvider tokenProvider;
    private final ExamService examService;
    private final ProblemSetItemRepository problemSetItemRepository;
    private final ProblemService problemService;

    @Transactional
    public EnterResponse execute(EnterRequest request) {
        // 1. 입장코드 검증
        EntryCode entryCode = entryCodeService.findByCode(request.code());
        entryCodeService.validateEntryCode(entryCode);

        // 2. 참가자 찾기 또는 생성
        User user = userService.findByPhone(request.phone());
        if (user == null) {
            user = userService.create(request.name(), request.phone());
        } else {
            // 기존 참가자 이름 업데이트 (필요시)
            if (!user.getName().equals(request.name())) {
                user.updateName(request.name());
            }
        }

        // 3. 시험 참가자 세션 찾기 또는 생성
        ExamParticipant examParticipant = examParticipantService.findByExamIdAndParticipantId(
                entryCode.getExamId(), user.getId());

        Assignment assignment = resolveAssignment(entryCode.getProblemSetId());

        if (examParticipant == null) {
            if (assignment.problemId() == null || assignment.specId() == null) {
                throw new RestApiException(ProblemErrorStatus.NO_ASSIGNED_PROBLEM);
            }
            examParticipant = examParticipantService.create(
                    entryCode.getExamId(),
                    user.getId(),
                    assignment.specId(),
                    entryCode.getTokenLimit(),
                    assignment.problemId()
            );
        } else if (examParticipant.getSpecId() == null || examParticipant.getAssignedProblemId() == null) {
            if (assignment.problemId() != null) {
                examParticipant.updateAssignedProblemId(assignment.problemId());
            }
            if (assignment.specId() != null) {
                examParticipant.updateSpecId(assignment.specId());
            }
            if (examParticipant.getSpecId() == null || examParticipant.getAssignedProblemId() == null) {
                throw new RestApiException(ProblemErrorStatus.NO_ASSIGNED_PROBLEM);
            }
        }

        // 4. JWT 생성
        String accessToken = tokenProvider.createAccessToken(
                user.getId().toString(), "USER");

        // 5. 입장코드 사용 횟수 증가 및 flush
        entryCodeService.incrementUsedCount(entryCode);
        examParticipantRepository.flush(); // 트랜잭션 커밋 전 flush

        // 6. Exam 정보 조회
        Exam exam = examService.findById(entryCode.getExamId());

        // 7. ResponseDTO 구성
        return new EnterResponse(
                accessToken,
                "USER",
                new EnterResponse.ParticipantInfo(
                        user.getId(),
                        user.getName(),
                        user.getPhone()
                ),
                new ExamInfoResponse(
                        exam.getId(),
                        exam.getTitle(),
                        exam.getState().name()
                ),
                new SessionInfoResponse(
                        examParticipant.getId(),
                        examParticipant.getTokenLimit(),
                        examParticipant.getTokenUsed()
                )
        );
    }

    private Assignment resolveAssignment(Long problemSetId) {
        if (problemSetId == null) {
            return new Assignment(null, null);
        }
        Long problemId = problemSetItemRepository.findByProblemSetId(problemSetId).stream()
                .findFirst()
                .map(ProblemSetItem::getProblemId)
                .orElse(null);
        if (problemId == null) {
            return new Assignment(null, null);
        }
        Problem problem = problemService.findById(problemId);
        return new Assignment(problemId, problem.getCurrentSpecId());
    }

    private record Assignment(Long problemId, Long specId) {
    }
}

