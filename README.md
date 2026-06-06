<div align="center">

# BE-VibeCodeEval

### AI 기반 코딩 테스트 평가 플랫폼 — Spring Boot 백엔드 서버

실시간 시험 관리, AI 채점 연동, WebSocket 브로드캐스트, SSE 스트리밍을 담당합니다.

<br>

[![Java](https://img.shields.io/badge/Java-17-007396?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=for-the-badge&logo=redis&logoColor=white)](https://redis.io/)

[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![Swagger](https://img.shields.io/badge/Swagger-OpenAPI_3-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)](https://swagger.io/)
[![Gradle](https://img.shields.io/badge/Gradle-8.5-02303A?style=for-the-badge&logo=gradle&logoColor=white)](https://gradle.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)](LICENSE)

</div>

---

## 목차

- [기술 스택](#-기술-스택)
- [아키텍처](#-아키텍처)
- [프로젝트 구조](#-프로젝트-구조)
- [주요 기능](#-주요-기능)
- [API 개요](#-api-개요)
- [인증 플로우](#-인증-플로우)
- [실시간 통신](#-실시간-통신)
- [시작하기](#-시작하기)
- [환경 변수](#-환경-변수)

---

## 🛠 기술 스택

<div align="center">

| 분류 | 기술 |
|:----:|:-----|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.2.0 |
| **Security** | Spring Security + JWT (HttpOnly Cookie) |
| **Database** | PostgreSQL 15 (JPA / Hibernate) |
| **Cache** | Redis 7 (Lettuce) |
| **Real-time** | STOMP WebSocket, SSE |
| **API Docs** | Springdoc OpenAPI (Swagger UI) |
| **ID Generation** | TSID (hypersistence-utils) |
| **Config Encryption** | Jasypt |
| **Monitoring** | Micrometer + Prometheus |
| **Build** | Gradle 8.5 |
| **Container** | Docker + Docker Compose |

</div>

---

## 🏗 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                        Cloudflare                           │
│              DNS · CDN · WAF · SSL/TLS                      │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTPS / WSS
                    ┌────▼────┐
                    │  Nginx  │  :443 → :8080 / :8001
                    └────┬────┘
           ┌─────────────┼─────────────┐
           │             │             │
    ┌──────▼──────┐      │      ┌──────▼──────┐
    │  BE Server  │      │      │  AI Worker  │
    │ Spring Boot │◄─────┘      │   FastAPI   │
    │   :8080     │◄────────────│   :8001     │
    └──────┬──────┘  Callback   └─────────────┘
           │
    ┌──────┴──────┐
    │  Data Layer │
    │ PostgreSQL  │
    │   Redis     │
    └─────────────┘
```

<br>

### 도메인 구성 (Clean Architecture)

```
domain/
├── auth        # 인증·인가 (JWT, 토큰 재발급)
├── exam        # 시험 세션 관리
├── submission  # 코드 제출 · Outbox Poller
├── chat        # AI 채팅 · 토큰 사용량
├── problem     # 문제 CRUD
├── statistics  # 통계 집계
└── admin       # 관리자 · 마스터 운영
```

각 도메인은 `ui → application → domain → infrastructure` 레이어로 분리되며,
UseCase 단위로 비즈니스 로직이 구성됩니다.

---

## 📁 프로젝트 구조

```
src/main/java/com/yd/vibecode/
├── VibecodeApplication.java
├── domain/
│   ├── admin/          # 관리자 계정·보드·메트릭·시험 관리
│   ├── auth/           # 로그인·로그아웃·토큰 재발급
│   ├── chat/           # 채팅 저장·조회·AI 콜백
│   ├── exam/           # 시험 상태·참가자 세션
│   ├── problem/        # 문제 조회
│   ├── statistics/     # 통계
│   └── submission/     # 제출·스트리밍·내부 처리
└── global/
    ├── config/         # Web, JPA, Redis, WebSocket, Async
    ├── security/       # SecurityConfig, JWT, STOMP Interceptor
    ├── interceptor/    # JWT Blacklist, Cookie Handshake
    ├── swagger/        # API 명세 인터페이스
    ├── exception/      # 전역 예외 처리
    ├── annotation/     # @CurrentUser, @AccessToken 등
    └── util/           # CookieUtils, SecureRandomGenerator
```

---

## ✨ 주요 기능

### 🔐 인증 / 인가

- **참가자 입장** — Entry Code + 이름 + 전화번호로 JWT 발급 (회원가입 불필요)
- **관리자 로그인** — ID/PW 기반 Access Token + Refresh Token 발급
- **토큰 저장** — HttpOnly 쿠키 전달 (XSS 방지)
- **토큰 로테이션** — Refresh Token 재발급 시 기존 토큰 즉시 무효화
- **블랙리스트** — 로그아웃된 Access Token을 Redis에 등록하여 재사용 차단
- **STOMP 인증 우회** — WebSocket은 HttpOnly 쿠키 접근 불가 → body로 토큰 전달 후 `StompPrincipalInterceptor`에서 검증

### 📝 시험 관리

- 시험 생성·시작·종료 (관리자)
- 시험 상태 실시간 조회 (타이머 동기화)
- 참가자 세션 정보 조회
- 코드 드래프트 자동 저장 (`PUT /api/exams/{id}/code-draft`)

### 🤖 코드 제출 · AI 채점

- 제출 접수 후 **202 Accepted** 즉시 응답 (비동기 처리)
- **Outbox Poller**로 Redis 이벤트를 AI Worker에 전달
- AI 채점 완료 시 Callback 수신 (`POST /api/callbacks/ai/**`)
- 채점 결과를 **SSE 스트림**으로 관리자에게 실시간 전송

### 📡 실시간 통신

- **STOMP WebSocket** (`/ws`) — 시험 상태 변경 브로드캐스트
- **SSE** (`/api/admin/submissions/{id}/stream`) — 채점 진행 스트리밍

### 💬 AI 채팅 연동

- 참가자 채팅 메시지 저장 → AI Worker로 전달
- 대화 이력 조회
- 토큰 사용량 추적 및 제한

### 🧑‍💼 관리자 / 마스터

- 관리자 계정 CRUD, 입장 코드 생성·관리
- 실시간 참가자 현황 보드
- 제출 상세 조회 (루브릭·코드 포함)
- 시스템 메트릭, 활동 로그
- 플랫폼 전역 설정 (시험 시간, 토큰 제한, 데이터 보관 정책)

---

## 📖 API 개요

<div align="center">

| Method | Path | 설명 | 권한 |
|:------:|:-----|:-----|:----:|
| `POST` | `/api/auth/enter` | 참가자 입장 (JWT 발급) | Public |
| `POST` | `/api/auth/admin/login` | 관리자 로그인 | Public |
| `POST` | `/api/auth/admin/logout` | 로그아웃 | ADMIN |
| `POST` | `/api/auth/admin/reissue` | 토큰 재발급 | Cookie |
| `GET` | `/api/auth/me` | 내 정보 조회 | Any |
| `GET` | `/api/exams/{id}/state` | 시험 상태 조회 | USER |
| `GET` | `/api/exams/{id}/participants/me` | 참가자 세션 조회 | USER |
| `POST` | `/api/exams/{id}/submissions` | 코드 제출 (202) | USER |
| `GET/PUT` | `/api/exams/{id}/code-draft` | 코드 드래프트 | USER |
| `GET` | `/api/submissions/{id}` | 제출 상세 (본인) | USER |
| `POST` | `/api/chat/messages` | 채팅 메시지 저장 | USER |
| `GET` | `/api/chat/history` | 채팅 이력 조회 | Any |
| `GET` | `/api/problems` | 문제 목록 조회 | USER |
| `GET` | `/api/admin/**` | 관리자 전용 API | ADMIN/MASTER |
| `POST` | `/api/callbacks/ai/**` | AI 채점 결과 수신 | Internal |
| `GET` | `/api/admin/submissions/{id}/stream` | 채점 결과 SSE | ADMIN |

</div>

> **Swagger UI** — `http://localhost:8080/swagger-ui/index.html`

---

## 🔑 인증 플로우

```
참가자                          서버
  │                              │
  ├─ POST /api/auth/enter ──────►│
  │  { entryCode, name, phone }  │
  │◄─────────────────────────────┤ Set-Cookie: accessToken (HttpOnly)
  │  { accessToken }             │ (body 포함: STOMP 인증용)
  │                              │
  │  STOMP Connect               │
  ├─ connectHeaders: {           │
  │    Authorization: Bearer ... │
  │  } ──────────────────────────►│ StompPrincipalInterceptor 검증
  │                              │

관리자
  │
  ├─ POST /api/auth/admin/login ─►│
  │◄─────────────────────────────┤ Set-Cookie: accessToken, refreshToken (HttpOnly)
  │                              │
  ├─ POST /api/auth/admin/reissue►│ refreshToken 검증 + 로테이션
  │◄─────────────────────────────┤ 새 accessToken, refreshToken 발급
```

---

## 📡 실시간 통신

### STOMP WebSocket

```
ws://host/ws

SUBSCRIBE /topic/exams/{examId}/state
→ 시험 시작·종료·상태 변경 브로드캐스트

SUBSCRIBE /topic/exams/{examId}/submissions
→ 신규 제출 알림 (관리자 보드)
```

### SSE (Server-Sent Events)

```
GET /api/admin/submissions/{submissionId}/stream

← data: {"status":"JUDGING","score":null}
← data: {"status":"COMPLETED","score":85}
← event: close
```

---

## 🚀 시작하기

### 사전 요구사항

- Java 17+
- Docker & Docker Compose

### 로컬 실행

```bash
# 1. 인프라 기동 (PostgreSQL + Redis)
docker compose up -d

# 2. 애플리케이션 실행
./gradlew bootRun

# 서버:   http://localhost:8080
# Swagger: http://localhost:8080/swagger-ui/index.html
```

### Docker 빌드

```bash
docker build -t be-vibecodeeval .
docker run -p 8080:8080 be-vibecodeeval
```

### 프로덕션 배포

```bash
docker compose -f docker-compose.prod.yml up -d
```

---

## ⚙️ 환경 변수

<div align="center">

| 변수 | 설명 | 예시 |
|:-----|:-----|:-----|
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/ai_vibe_coding_test` |
| `SPRING_DATASOURCE_USERNAME` | DB 사용자명 | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | DB 비밀번호 | `password` |
| `SPRING_REDIS_HOST` | Redis 호스트 | `localhost` |
| `SPRING_REDIS_PORT` | Redis 포트 | `6379` |
| `JWT_SECRET` | JWT 서명 키 (256bit+) | `your-secret-key` |
| `JWT_ACCESS_EXPIRATION` | Access Token 만료 (ms) | `3600000` |
| `JWT_REFRESH_EXPIRATION` | Refresh Token 만료 (ms) | `604800000` |
| `JASYPT_ENCRYPTOR_PASSWORD` | 설정 파일 암호화 키 | `jasypt-password` |
| `AI_SERVER_URL` | AI Worker 주소 | `http://localhost:8001` |

</div>

---

## 🔗 관련 레포지토리

<div align="center">

| 레포 | 설명 |
|:----:|:-----|
| [FE-VibeCodeEval](../FE-VibeCodeEval) | Next.js 프론트엔드 (User / Admin / Master UI) |
| [AI-VibeCodeEval](../AI-VibeCodeEval) | FastAPI + LangGraph AI 채점 워커 |

</div>

<br>

> 전체 시스템 구성도(Mermaid)는 [`docs/architecture.md`](../docs/architecture.md)를 참조하세요.
