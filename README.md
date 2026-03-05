# Alrimjang

조직 공지 · 실시간 채팅 · 할 일 · 설문을 통합한 Spring Boot 기반 협업 시스템입니다.

## 주요 기능

### 공지
- 대상 지정 배포 (전체 / 역할별 / 그룹별), 수신/읽음 2단계 추적
- 관리자 숨김/복구, 중요 공지 상단 고정
- 제목/작성자/날짜/전체 동적 검색 (MyBatis `<choose>/<when>` + PostgreSQL `ILIKE`)
- 페이지네이션 (offset/limit 기반, `PageRequest`/`PageResult` 공통 모델)

### 채팅
- 1:1 DM / 그룹 채팅 (WebSocket STOMP 프로토콜)
- DM 방 ID: `dm__{tokenA}__{tokenB}` — 양쪽 토큰 비교로 접근 제어
- 그룹 방 ID: `grp__{groupId}` — `group_members` 소속 + `type='CHAT'` 검증
- 안읽은 메시지 카운트 (`chat_room_reads.last_read_at` 기준 집계)
- 상대방 읽음 시각 조회 (DM 전용 `readStatus` API)
- 채팅방 목록: CTE + `DISTINCT ON` 단일 쿼리로 방별 최근 메시지/할 일/안읽음 수 일괄 조회

### 채팅 할 일
- DM 방 내 할 일 생성 (제목, 설명, 마감일, 담당자 지정)
- 상태 전이: `PENDING → DONE → CONFIRMED` (DB `WHERE status =` 조건부 UPDATE)
- 담당자만 완료(`DONE`), 생성자만 확인(`CONFIRMED`) 가능 — 권한 분리
- 할 일 목록 정렬: `PENDING` 우선 → 마감일 오름차순 → 생성일 내림차순

### 설문
- 관리자 설문 생성 (제목, 설명, 시작/마감 기간, 문항 동적 추가)
- 문항 유형: `TEXT`(주관식), `SINGLE`(단일선택), `MULTI`(복수선택)
- 응답 5단계 무결성 검증: 기간 → 중복응답(UNIQUE 제약) → 필수문항 → 단일선택 개수 → 유효 옵션 ID
- 관리자 결과 집계: 객관식 투표수/비율(`LEFT JOIN + GROUP BY`), 주관식 최근 20건

### 권한
- `ROLE_ADMIN`: `/admin/**` 전체 접근
- `ROLE_NOTICE_WRITER` 또는 `ROLE_ADMIN`: 공지 작성/수정/삭제
- `ROLE_NOTIFICATION_WRITER` 또는 `ROLE_ADMIN`: 알림 작성
- 공지 수정: 작성자 본인만 가능 (`author_id` 비교)
- 공지 삭제: 작성자 본인 또는 ADMIN
- 공지 숨김/해제: ADMIN 전용
- 채팅방 접근: `ChatRoomAccessService`에서 DM/그룹 규칙 통합 검증

## 기술 스택

| 구분 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.2.2, Spring Security 6 |
| ORM | MyBatis 3.0.3 (XML Mapper 8개) |
| DB | PostgreSQL (Neon Cloud Serverless), Flyway 마이그레이션 |
| 실시간 통신 | WebSocket + STOMP, SockJS 폴백 |
| View | Thymeleaf, Bootstrap 5, jQuery |
| 테스트 | JUnit 5, Mockito |
| 배포 | Docker, Render |

## 아키텍처

```text
[Browser]
   |  HTTP / WebSocket
   v
[Controller]  ── 9개: Auth, Notice, Chat, Survey, Admin, Home, Register, NoticeAudience, UserNoticeReceipt
   |  Service 호출
   v
[Service]     ── 7개: Notice, NoticeAudience, Chat, Survey, User, ChatRoomAccess, CustomUserDetails
   |  비즈니스 규칙/권한 검증/트랜잭션
   v
[MyBatis Mapper]  ── 인터페이스 8개 + XML 8개
   |  SQL
   v
[PostgreSQL]  ── 12 테이블, 15 FK, 12 인덱스
```

### 레이어 책임
- **Controller**: 요청/응답 변환, 인증 사용자 식별 (`Principal`). 비즈니스 로직 금지
- **Service**: 도메인 규칙, 권한/접근 제어, 상태 전이, 검증. Service 인터페이스 + Impl 구현체 분리
- **Mapper**: SQL은 XML에 분리, 인터페이스로만 호출. ResultMap으로 Snake → Camel 매핑

### WebSocket 구조
```text
[Client]
   ├── SUBSCRIBE /topic/chat.{roomId}
   │       → ChatStompAccessInterceptor.preSend()
   │           → ChatRoomAccessService.assertAccessible()
   │
   └── SEND /app/chat/send
           → ChatController.send()
               → ChatRoomAccessService.assertAccessible()
               → ChatService.send()  →  DB INSERT
               → messagingTemplate.convertAndSend(/topic/chat.{roomId})
```

### Security 구조
```text
[HTTP 요청]
   → SecurityFilterChain
       → CSRF: /ws-chat/**, /api/chat/** 제외
       → URL 패턴별 권한: ADMIN, NOTICE_WRITER, NOTIFICATION_WRITER
       → formLogin → CustomUserDetailsService → SecurityContext
       
[세션 이벤트]
   → SecurityActivityListener
       → AuthenticationSuccess → ActiveUserTracker.markActive()
       → LogoutSuccess / SessionDestroyed → markInactive()
```

## ERD (실제 테이블 기준)

```mermaid
erDiagram
    users ||--o{ group_members : belongs
    groups ||--o{ group_members : has

    users ||--o{ notice : writes
    notice ||--o{ notice_targets : has
    notice ||--o{ notice_receipts : delivered
    users ||--o{ notice_receipts : receives

    users ||--o{ chat_messages : sends
    users ||--o{ chat_room_reads : reads
    users ||--o{ chat_tasks : "creates / assigned"

    users ||--o{ surveys : creates
    surveys ||--o{ survey_questions : has
    survey_questions ||--o{ survey_options : has
    surveys ||--o{ survey_responses : has
    users ||--o{ survey_responses : submits
    survey_responses ||--o{ survey_answers : has
    survey_questions ||--o{ survey_answers : answered
    survey_options ||--o{ survey_answers : selected
```

### 테이블 상세

| 테이블 | 주요 컬럼 | 설명 |
|--------|-----------|------|
| `users` | id, username, password, name, role, enabled, can_post_notice, can_post_notification | 사용자. 역할(ADMIN/USER) + 세부 권한 플래그 |
| `groups` | id, org_id, code, name, type | 그룹. `type='CHAT'`이면 채팅방 |
| `group_members` | group_id, user_id (PK) | 그룹 소속 |
| `notice` | id, title, content, author_id, author_name, is_important, is_hidden, view_count | 공지. 중요/숨김 상태 |
| `notice_targets` | id, notice_id, target_type(ALL/ROLE/GROUP), target_value | 공지 배포 대상 |
| `notice_receipts` | id, notice_id, user_id, delivered_at, read_at, UNIQUE(notice_id, user_id) | 수신/읽음 추적. 배포와 열람 분리 |
| `chat_messages` | id, room_id, sender_id, sender_name, content, created_at | 채팅 메시지 |
| `chat_room_reads` | user_id, room_id (PK), last_read_at | 읽음 시각. UPSERT(`ON CONFLICT DO UPDATE`) |
| `chat_tasks` | id, room_id, title, description, deadline_at, status(PENDING/DONE/CONFIRMED), created_by_*, assignee_*, completed_at, confirmed_at | 할 일. 생성자/담당자 분리 |
| `surveys` | id, title, description, status, start_at, end_at, created_by_* | 설문 |
| `survey_questions` | id, survey_id, seq, type(TEXT/SINGLE/MULTI), title, is_required | 문항 |
| `survey_options` | id, question_id, seq, option_label | 선택지 |
| `survey_responses` | id, survey_id, user_id, username, user_name, UNIQUE(survey_id, user_id) | 응답 헤더. DB레벨 중복 방지 |
| `survey_answers` | id, response_id, survey_id, question_id, option_id, answer_text | 개별 답변 (객관식: option_id, 주관식: answer_text) |

### 인덱스 설계

| 인덱스 | 대상 컬럼 | 용도 |
|--------|-----------|------|
| `idx_notice_created_at` | notice(created_at DESC) | 공지 목록 최신순 |
| `idx_notice_important_created` | notice(is_important DESC, created_at DESC) | 중요 공지 상단 고정 |
| `idx_notice_targets_type_value` | notice_targets(target_type, target_value) | 수신자 추출 필터 |
| `idx_notice_receipts_user_delivered` | notice_receipts(user_id, delivered_at DESC) | 내 수신 공지 목록 |
| `idx_chat_messages_room_created` | chat_messages(room_id, created_at DESC) | 방별 최근 메시지, DISTINCT ON 지원 |
| `idx_chat_tasks_room_deadline` | chat_tasks(room_id, deadline_at ASC) | 방별 할 일 마감순 |
| `idx_chat_tasks_assignee_status` | chat_tasks(assignee_user_id, status, deadline_at ASC) | 담당자별 미완료 할 일 |
| `idx_surveys_status_created` | surveys(status, created_at DESC) | PUBLISHED 설문 목록 |
| `idx_survey_questions_survey_seq` | survey_questions(survey_id, seq) | 문항 순서 조회 |
| `idx_survey_responses_survey` | survey_responses(survey_id, submitted_at DESC) | 설문별 응답 목록 |

## 핵심 쿼리 패턴

### 1. 공지 수신자 추출 — 3중 EXISTS 서브쿼리
```sql
SELECT DISTINCT u.id FROM users u
WHERE u.enabled = TRUE AND (
    EXISTS (SELECT 1 FROM notice_targets nt WHERE nt.notice_id = ? AND nt.target_type = 'ALL')
    OR EXISTS (SELECT 1 FROM notice_targets nt WHERE ... AND nt.target_type = 'ROLE' AND nt.target_value = u.role)
    OR EXISTS (SELECT 1 FROM notice_targets nt JOIN group_members gm ... WHERE nt.target_type = 'GROUP' AND gm.user_id = u.id)
)
```
대상 조건(ALL/ROLE/GROUP)을 `notice_targets`에서 판별하고, 해당 조건에 맞는 활성 사용자를 한 번에 추출합니다.

### 2. 채팅방 목록 — CTE + DISTINCT ON
```sql
WITH my_rooms AS (...),
     last_message AS (SELECT DISTINCT ON (m.room_id) ... ORDER BY m.room_id, m.created_at DESC),
     last_task AS (SELECT DISTINCT ON (t.room_id) ... ORDER BY t.room_id, t.created_at DESC),
     room_base AS (... LEFT JOIN last_message LEFT JOIN last_task)
SELECT room_id, last_message, unread_count FROM room_base ORDER BY last_message_at DESC
```
4개의 CTE로 방 목록 → 최근 메시지 → 최근 할 일 → 병합을 구성하고, 안읽은 카운트를 서브쿼리로 계산합니다.

### 3. 채팅 읽음 UPSERT
```sql
INSERT INTO chat_room_reads (room_id, user_id, last_read_at) VALUES (?, ?, ?)
ON CONFLICT (user_id, room_id) DO UPDATE SET last_read_at = EXCLUDED.last_read_at
```

### 4. 설문 투표 통계 집계
```sql
SELECT o.id, o.option_label, COALESCE(COUNT(a.id), 0) AS vote_count
FROM survey_options o LEFT JOIN survey_answers a ON a.option_id = o.id AND a.question_id = o.question_id
WHERE o.question_id = ? GROUP BY o.id, o.question_id, o.seq, o.option_label ORDER BY o.seq
```

### 5. 할 일 상태 전이 — 조건부 UPDATE
```sql
-- 완료: PENDING 상태인 경우만 DONE으로 전이
UPDATE chat_tasks SET status = 'DONE', completed_at = ? WHERE id = ? AND room_id = ? AND status = 'PENDING'

-- 확인: DONE 상태인 경우만 CONFIRMED로 전이
UPDATE chat_tasks SET status = 'CONFIRMED', confirmed_at = ? WHERE id = ? AND room_id = ? AND status = 'DONE'
```

### 6. 동적 검색 — MyBatis choose/when
```xml
<choose>
    <when test="searchType == 'title'">AND title ILIKE CONCAT('%', #{keyword}, '%')</when>
    <when test="searchType == 'author'">AND author_name ILIKE ...</when>
    <when test="searchType == 'createdAt'">AND TO_CHAR(created_at, 'YYYY-MM-DD') ILIKE ...</when>
    <otherwise>AND (title ILIKE ... OR content ILIKE ... OR author_name ILIKE ...)</otherwise>
</choose>
```

## 주요 엔드포인트

### 공지

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/notices` | 공지 목록 (페이지네이션 + 검색) |
| GET | `/notices/{id}` | 공지 상세 + 조회수 증가 |
| POST | `/notices` | 공지 작성 (NOTICE_WRITER) |
| POST | `/notices/{id}` | 공지 수정 (작성자 본인) |
| POST | `/notices/{id}/delete` | 공지 삭제 (작성자 or ADMIN) |
| POST | `/notices/{id}/hide` | 공지 숨김 (ADMIN) |
| POST | `/notices/{id}/unhide` | 공지 해제 (ADMIN) |
| POST | `/notices/{id}/deliver` | 공지 배포 (수신 레코드 생성) |
| POST | `/api/notices/{id}/read` | 공지 읽음 처리 |
| GET | `/api/users/me/receipts` | 내 수신 공지 목록 |

### 채팅

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/chat` | 채팅 메인 페이지 |
| GET | `/api/chat/rooms` | 내 채팅방 목록 (요약 + 안읽음) |
| GET | `/api/chat/rooms/{roomId}/history` | 메시지 이력 (limit clamp 200) |
| GET | `/api/chat/rooms/{roomId}/unread-count` | 안읽은 메시지 수 |
| POST | `/api/chat/rooms/{roomId}/read` | 읽음 처리 |
| GET | `/api/chat/rooms/{roomId}/read-status` | 상대방 읽음 시각 |
| GET | `/api/chat/rooms/{roomId}/tasks` | 할 일 목록 |
| POST | `/api/chat/rooms/{roomId}/tasks` | 할 일 생성 |
| POST | `/api/chat/rooms/{roomId}/tasks/{taskId}/done` | 할 일 완료 (담당자) |
| POST | `/api/chat/rooms/{roomId}/tasks/{taskId}/confirm` | 할 일 확인 (생성자) |
| GET | `/api/chat/direct/{username}` | DM 방 조회/생성 |
| POST | `/api/chat/groups` | 그룹 채팅방 생성 |
| WS | `/app/chat/send` | 메시지 전송 (STOMP) |
| WS | `/topic/chat.{roomId}` | 메시지 실시간 수신 (STOMP) |

### 설문

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/surveys` | 설문 목록 (PUBLISHED, 응답여부 표시) |
| GET | `/surveys/{id}` | 설문 응답 페이지 (기간/중복 검증) |
| POST | `/surveys/{id}/responses` | 설문 응답 제출 (5단계 검증) |

### 관리자

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/admin` | 관리자 대시보드 |
| GET | `/admin/users` | 사용자 관리 |
| GET | `/admin/permissions` | 권한 관리 |
| POST | `/admin/noticewriters` | 공지/알림 작성 권한 일괄 변경 |
| GET | `/admin/notifications/new` | 알림 작성 폼 |
| POST | `/admin/notifications` | 알림 작성 + 배포 (대상 설정 → 배포 트랜잭션) |
| GET | `/admin/surveys/new` | 설문 작성 폼 |
| POST | `/admin/surveys` | 설문 생성 |
| GET | `/admin/surveys/results` | 설문 결과 목록 |
| GET | `/admin/surveys/{id}/results` | 설문 결과 상세 (투표율/주관식 집계) |

## DB 마이그레이션
- Flyway 사용 (`src/main/resources/db/migration`)
- 초기 스키마: `V1__init_schema.sql` (12 테이블 + 15 FK + 12 인덱스)
- 기존 DB에 히스토리 테이블이 없으면 `baseline-on-migrate`로 베이스라인 처리
- 운영 런북: [`docs/DB_MIGRATION_RUNBOOK.md`](docs/DB_MIGRATION_RUNBOOK.md)
- 사전 점검 SQL: [`scripts/db-preflight-check.sql`](scripts/db-preflight-check.sql)

## 테스트

| 테스트 클래스 | 검증 범위 |
|---------------|-----------|
| `ChatRoomAccessServiceTest` | DM/그룹방 접근 허용·차단, 잘못된 roomId 포맷, null/blank 입력 |
| `ChatServiceImplTest` | 메시지 전송, 할 일 생성 검증(Room 유효성/담당자 존재), 상태 전이(PENDING→DONE→CONFIRMED), 권한 없는 전이 차단 |
| `NoticeServiceImplTest` | 공지 CRUD, 작성자별 수정/삭제 권한, ADMIN 숨김/해제, 존재하지 않는 공지 예외 |
| `SurveyServiceImplTest` | 설문 생성, 응답 제출 무결성(기간/필수/단일선택/유효ID/중복), 관리자 결과 집계 |

## 권한 정책

| 대상 | 권한 조건 |
|------|-----------|
| `/admin/**` | `ROLE_ADMIN` |
| 공지 작성/수정/삭제 | `ROLE_NOTICE_WRITER` 또는 `ROLE_ADMIN` |
| 알림 작성 | `ROLE_NOTIFICATION_WRITER` 또는 `ROLE_ADMIN` |
| DM 방 접근 | 방 토큰에 본인 username 포함 |
| 그룹 방 접근 | `group_members` 소속 + 그룹 `type='CHAT'` |
| 할 일 완료 | `assigneeUsername` 일치 |
| 할 일 확인 | `createdByUsername` 일치 |
| 공지 숨김/해제 | `ROLE_ADMIN` |

## 성능/트래픽 기준
- 메시지 조회 API: 최대 `200`건 상한 처리 (`Math.min(limit, 200)`)
- 채팅방 목록: N+1 → CTE 단일 쿼리로 최적화
- 안읽음 카운트: `chat_room_reads.last_read_at` 기반 `WHERE m.created_at > r.last_read_at` 집계
- 핵심 컬럼에 인덱스 12개 구성 (채팅/설문/공지 조회 최적화)
- 수신 레코드 INSERT: `ON CONFLICT DO NOTHING`으로 중복 배포 방지
- 현재 단계는 기능/정합성 중심, 대규모 부하테스트(k6/JMeter) 수치 미포함

## 실행 방법

### 1) 환경 변수 준비
`.env.example`을 기준으로 환경 변수 설정:

```env
DB_URL=jdbc:postgresql://localhost:5432/alrimjang
DB_USERNAME=alrimjang_app
DB_PASSWORD=change_me
PORT=9090
SPRING_PROFILES_ACTIVE=dev
```

### 2) 실행
```bash
mvn spring-boot:run
```

### 3) 테스트
```bash
mvn test
```

## 트러블슈팅
1. `can_post_notification` 컬럼 누락으로 관리자 페이지 500
   - 조치: Flyway 마이그레이션에 `ALTER TABLE` 반영 + DB 사전 점검 스크립트 도입

2. 설문 Mapper XML 파싱 오류
   - 원인: XML 특수문자(`<`, `>`) 미이스케이프, 태그 불일치
   - 조치: `&lt;` `&gt;` 사용 + 부팅 시점 XML 검증 필수화

3. 채팅방 접근 제어 누락
   - 원인: 방 ID만으로 진입 가능한 UI + STOMP 구독 무검증
   - 조치: `ChatRoomAccessService` + `ChatStompAccessInterceptor` 도입

## 포트폴리오 포인트
- 단순 CRUD가 아닌 **권한/접근제어/상태전이/실시간 처리/무결성 검증**을 포함한 프로젝트
- Service 인터페이스 + Impl 분리, MyBatis XML Mapper로 SQL 직접 제어
- Flyway로 DB 스키마 이력 추적, 테스트 코드로 핵심 정책 회귀 검증
- WebSocket STOMP + `ChannelInterceptor`로 실시간 통신 접근 제어 구현
