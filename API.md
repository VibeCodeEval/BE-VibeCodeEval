AI Vibe Coding Test — API 명세 요약 (학습용 Markdown)

본 문서는 모델 학습/임베딩에 최적화된 요약본입니다.
역할/인증, 상태기계, 엔드포인트별 요약 목적 → 핵심 파라미터/바디 → 응답 스키마 핵심 필드 → 부작용/연계 테이블 순으로 정리했습니다.

0) 공통 규칙
인증/권한

Authorization: Bearer <JWT>

role 클레임: USER | ADMIN

서비스 간 호출(EVAL): X-Core-Auth: <service-secret>

공통 ID & 상태

examId, participantId, examParticipantId, submissionId, problemId, specVersion, spec_id

시험 상태: WAITING | RUNNING | ENDED

제출 상태: QUEUED | RUNNING | DONE | FAILED

테스트 그룹: SAMPLE | PUBLIC | PRIVATE

채점 판정: AC | WA | TLE | MLE | RE

점수 산식(요구사항 고정)

total = prompt(40) + perf(30) + correctness(30)

1) AUTH
1.1 입장(사용자)

POST /api/auth/enter
목적: 입장코드 검증 + 참가자 업서트 + 세션/토큰 초기화 + JWT 발급
Body

{ "code":"JOIN-2025-ABCD", "name":"홍길동", "phone":"010-1234-5678" }


Response 핵심

{
  "accessToken":"jwt.user...",
  "role":"USER",
  "participant":{"id":102,"name":"홍길동","phone":"010-1234-5678"},
  "exam":{"id":501,"title":"AI 바이브 코딩 테스트","state":"WAITING"},
  "session":{"examParticipantId":9001,"tokenLimit":20000,"tokenUsed":0}
}


연계 테이블: entry_codes, participants, exam_participants

1.2 관리자 생성/로그인

POST /api/auth/admin/signup → 관리자 생성(넘버+SecretKey)

POST /api/auth/admin/login → 관리자 JWT 발급

1.3 세션 복원

GET /api/auth/me
응답 핵심: role, participant, exam.state, session(tokenLimit/tokenUsed/assignedSpecVersion/assignedProblemId)

2) EXAM (시험 제어/타이머)
2.1 사용자 상태 동기화

GET /api/exams/{examId}/state (USER)
응답 핵심

{
  "examId":501, "state":"RUNNING",
  "startAt":"2025-10-26T06:00:00Z", "endAt":"2025-10-26T07:00:00Z",
  "serverTime":"2025-10-26T06:15:01Z", "version":7
}


→ 클라이언트 타이머는 서버 시각 기준.

2.2 관리자 시험 제어

POST /api/admin/exams/{id}/start → 상태 RUNNING 전환, WS 브로드캐스트

POST /api/admin/exams/{id}/end → 상태 ENDED 전환

POST /api/admin/exams/{id}/extend { "minutes": 10 } → 종료시각 연장

연계 테이블: exams(state/version), exam_participants(state)

3) PROBLEM (배정/스펙 잠금)
3.1 배정 문제 & 잠금 스펙 조회

GET /api/exams/{examId}/assignment (USER)
응답 핵심

{
  "problem":{"id":301,"title":"최단 경로의 합","contentMd":"...","tags":["graph","dp"],"difficulty":"MEDIUM"},
  "spec":{
    "version":3,
    "limits":{"timeMs":2000,"memoryMb":512},
    "restrictions":{"allowedLangs":["cpp17","python3.11"],"forbiddenApis":[]},
    "checker":{"type":"equality"}
  }
}


규칙: 세션 시작 시 spec_id/spec.version 잠금. (대화/채점/평가 모두 동일 스펙 참조)
연계 테이블: problems, problem_specs, exam_participants(spec_id)

4) SUBMISSION (제출/채점/SSE)
4.1 제출

POST /api/exams/{examId}/submissions (USER)
Body

{ "lang":"python3.11","code":"print('hello')" }


Response: 202 Accepted

{ "submissionId":88001,"status":"QUEUED" }


부작용: judge_queue enqueue, submissions 생성(SHA256/LOC/bytes)

4.2 채점 이벤트 스트림(SSE)

GET /api/submissions/{submissionId}/events (SSE)
이벤트 타입 & 페이로드

build → { "stage":"build","ok":true }

case_start(선택)

case_end → { "caseId":12,"group":"PRIVATE","verdict":"AC","timeMs":41,"memKb":8120 }

summary → 집계 요약

score → { "prompt":33.0,"perf":24.5,"correctness":28.0,"total":85.5 }

4.3 제출 상세 조회

GET /api/submissions/{submissionId}
응답 핵심

{
  "id":88001,"status":"DONE","lang":"python3.11",
  "metrics":{"timeMsMedian":41,"memKbPeak":8120,"loc":12},
  "tc":{"passRateWeighted":0.92,"groups":[{"name":"SAMPLE","pass":3,"total":3,"weight":0.1}]},
  "score":{"prompt":33.0,"perf":24.5,"correctness":28.0,"total":85.5}
}


연계 테이블: submissions, submission_runs, scores

5) ADMIN (운영)
5.1 입장코드

POST /api/admin/entry-codes
Body: label, examId, problemSetId, expiresAt, maxUses

GET /api/admin/entry-codes?examId=...

PATCH /api/admin/entry-codes/{code} { "isActive": false }

5.2 수험자 보드/상태판

GET /api/admin/board?examId=...
→ 이름/전화 마스킹, 진행상태, 토큰잔량, 제출여부

GET /api/admin/metrics
응답 핵심(런타임)

{
  "concurrency":{"activeExaminees":120,"wsConnections":130},
  "queue":{"judgeQueueDepth":4,"avgWaitSec":2.3},
  "errors":{"rate1m":0.2,"last":"NONE"}
}


비고: 운영지표의 영속 스냅샷은 exam_statistics(ERD 추가)로 보관:

최근 버킷: SELECT ... FROM exam_statistics WHERE exam_id=$1 ORDER BY bucket_start DESC LIMIT 1;

5.3 관리자 보안/체인지로그

PATCH /api/admin/account/password { "currentPassword":"...", "newPassword":"..." }

GET /api/admin/problems/{problemId}/specs
→ 버전/체인지로그 열람(수험자 UI 비노출)

6) AI (FASTAPI)
6.1 CHAT — WebSocket 토큰 스트리밍

WS wss://ai.example.com/chat
쿼리/컨텍스트: examParticipantId, problemId, specVersion, tokenBudget (+ USER JWT 헤더)
Client → Server

{ "type":"user_message","turnId":"t-123","message":"DP로 푸는 법?" }


취소:

{ "type":"cancel","turnId":"t-123" }


Server → Client

{ "type":"start","turnId":"t-123","model":"gpt-x" }
{ "type":"delta","turnId":"t-123","index":0,"content":"다음과 같이..." }
{ "type":"done","turnId":"t-123","usage":{"prompt":123,"completion":456,"total":579},"finishReason":"stop" }


부작용/규칙

대화 저장: prompt_sessions / prompt_messages

usage 콜백 → Core에 전달 → exam_participants.token_used 누적(한도 초과 시 대화 차단)

6.2 EVALUATION — 프롬프트 평가

POST https://ai.example.com/evaluate/prompt
Headers: Content-Type: application/json, X-Core-Auth: <service-secret>
Body 요약

{
  "examParticipantId":9001,
  "problemId":301,
  "spec":{"version":3,"limits":{"timeMs":2000,"memoryMb":512},"constraints":["I/O","금지API: os.popen"]},
  "messages":[{"role":"USER","content":"요구사항 정리"},{"role":"AI","content":"..."}]
}


Response 핵심

{
  "scores":{"specCompleter":9.0,"ruleBinder":8.0,"docSync":7.5,"taskSplitter":8.5},
  "total":33.0,
  "rubric":{"specCompleter":{"missing":[],"hit":["입력/출력/제약"]},"docSync":{"latest":true}}
}


연계: Core가 scores/메타를 제출 단위에 저장(요구사항 P0)

7) 오류 코드(공통)
code	의미	예시 메시지
INVALID_CODE	입장코드 무효	"입장코드가 유효하지 않습니다"
CODE_EXPIRED	입장코드 만료	"만료된 코드"
CODE_CAP_REACHED	최대 사용량 초과	"정원 초과"
UNAUTHORIZED	토큰 없음/만료	"로그인이 필요합니다"
FORBIDDEN	권한 부족	"ADMIN 권한 필요"
TOKEN_LIMIT_EXCEEDED	토큰 한도 초과	"잔여 토큰 부족"

8) 최소 예시(cURL 스니펫)
로그인/입장
curl -X POST https://core/api/auth/enter \
  -H "Content-Type: application/json" \
  -d '{ "code":"JOIN-2025-ABCD","name":"홍길동","phone":"010-1234-5678" }'

배정 문제
curl -H "Authorization: Bearer $USER_JWT" \
  https://core/api/exams/501/assignment

제출 + SSE
# 제출
curl -X POST https://core/api/exams/501/submissions \
  -H "Authorization: Bearer $USER_JWT" -H "Content-Type: application/json" \
  -d '{ "lang":"python3.11","code":"print(42)" }'

# SSE (브라우저/CLI는 이벤트 스트림 지원 도구 사용)
curl -H "Accept: text/event-stream" -H "Authorization: Bearer $USER_JWT" \
  https://core/api/submissions/88001/events

관리자 메트릭(런타임) & 통계(영속)
# 런타임 현황
curl -H "Authorization: Bearer $ADMIN_JWT" https://core/api/admin/metrics

# 영속 통계(ERD: exam_statistics)
# SELECT * FROM exam_statistics WHERE exam_id=501 ORDER BY bucket_start DESC LIMIT 1;

9) 설계 포인트(학습 키워드)

세션 스펙 잠금: exam_participants.spec_id → 대화/제출/평가의 단일 기준

비동기 채점: 202 Accepted + SSE(build/case_end/summary/score)

WS 스트리밍: Chat 토큰 델타, usage 콜백으로 토큰 한도 관리

평가 4요소: 명세충실도/규칙화/최신문서반영/작업분해 (+근거 JSON)

운영 지표: /api/admin/metrics(런타임) ↔ exam_statistics(영속 버킷 스냅샷)