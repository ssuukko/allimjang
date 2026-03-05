# DB Migration Runbook (Flyway)

## 대상
- 프로젝트: Alrimjang
- DB: PostgreSQL
- 마이그레이션: `src/main/resources/db/migration/V1__init_schema.sql`

## 1) 배포 전 백업

### 1-1. 전체 백업 (권장)
```bash
export PGPASSWORD='YOUR_DB_PASSWORD'
pg_dump \
  --host YOUR_DB_HOST \
  --port 5432 \
  --username YOUR_DB_USER \
  --dbname YOUR_DB_NAME \
  --format custom \
  --file backup_before_flyway_$(date +%Y%m%d_%H%M%S).dump
```

### 1-2. 스키마-only 백업 (보조)
```bash
export PGPASSWORD='YOUR_DB_PASSWORD'
pg_dump \
  --host YOUR_DB_HOST \
  --port 5432 \
  --username YOUR_DB_USER \
  --dbname YOUR_DB_NAME \
  --schema-only \
  --file schema_before_flyway_$(date +%Y%m%d_%H%M%S).sql
```

## 2) 배포 전 점검
1. [`scripts/db-preflight-check.sql`](/mnt/d/ssh/allimjang/scripts/db-preflight-check.sql) 실행
2. 결과에서 `missing_*` 항목이 없어야 함
3. 앱 환경변수 확인
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

## 3) 배포 절차
1. 애플리케이션 배포
2. 앱 시작 시 Flyway 자동 수행
3. 검증 쿼리
```sql
SELECT installed_rank, version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

## 4) 실패 시 롤백
Flyway Community는 자동 rollback(undo)을 제공하지 않으므로, 백업 복구 방식으로 롤백합니다.

### 4-1. 앱 중지
- 트래픽 차단 후 애플리케이션 중지

### 4-2. 백업 복구
```bash
export PGPASSWORD='YOUR_DB_PASSWORD'
pg_restore \
  --host YOUR_DB_HOST \
  --port 5432 \
  --username YOUR_DB_USER \
  --dbname YOUR_DB_NAME \
  --clean \
  --if-exists \
  --no-owner \
  backup_before_flyway_YYYYMMDD_HHMMSS.dump
```

### 4-3. 재기동 전 확인
```sql
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM notice;
SELECT COUNT(*) FROM chat_messages;
```

## 5) 운영 체크리스트
- 배포 전 백업 파일 생성/보관 확인
- 점검 SQL 결과 정상 확인
- `flyway_schema_history` 성공 여부 확인
- 권한 기능(관리자, 공지작성, 알림작성) 수동 점검
- 채팅방 접근 제어 수동 점검
