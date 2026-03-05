-- PostgreSQL preflight check for Alrimjang schema
-- 실행 예시: psql "$DB_URL" -f scripts/db-preflight-check.sql

-- 1) 필수 테이블 누락 여부
WITH expected(table_name) AS (
    VALUES
        ('users'),
        ('groups'),
        ('group_members'),
        ('notice'),
        ('notice_targets'),
        ('notice_receipts'),
        ('chat_messages'),
        ('chat_room_reads'),
        ('chat_tasks'),
        ('surveys'),
        ('survey_questions'),
        ('survey_options'),
        ('survey_responses'),
        ('survey_answers')
)
SELECT e.table_name AS missing_table
FROM expected e
LEFT JOIN information_schema.tables t
       ON t.table_schema = 'public'
      AND t.table_name = e.table_name
WHERE t.table_name IS NULL
ORDER BY e.table_name;

-- 2) users 필수 컬럼 누락 여부
WITH expected(column_name) AS (
    VALUES
        ('id'),
        ('username'),
        ('password'),
        ('name'),
        ('role'),
        ('created_at'),
        ('enabled'),
        ('can_post_notice'),
        ('can_post_notification')
)
SELECT e.column_name AS missing_users_column
FROM expected e
LEFT JOIN information_schema.columns c
       ON c.table_schema = 'public'
      AND c.table_name = 'users'
      AND c.column_name = e.column_name
WHERE c.column_name IS NULL
ORDER BY e.column_name;

-- 3) 핵심 FK 누락 여부
WITH expected(conname) AS (
    VALUES
        ('fk_group_members_group'),
        ('fk_group_members_user'),
        ('fk_notice_targets_notice'),
        ('fk_notice_receipts_notice'),
        ('fk_notice_receipts_user'),
        ('fk_chat_messages_sender'),
        ('fk_chat_room_reads_user'),
        ('fk_chat_tasks_created_by_user'),
        ('fk_chat_tasks_assignee_user'),
        ('fk_surveys_created_by_user'),
        ('fk_survey_questions_survey'),
        ('fk_survey_options_question'),
        ('fk_survey_responses_survey'),
        ('fk_survey_responses_user'),
        ('fk_survey_answers_response'),
        ('fk_survey_answers_survey'),
        ('fk_survey_answers_question'),
        ('fk_survey_answers_option')
)
SELECT e.conname AS missing_fk
FROM expected e
LEFT JOIN pg_constraint c ON c.conname = e.conname
WHERE c.oid IS NULL
ORDER BY e.conname;

-- 4) 핵심 인덱스 누락 여부
WITH expected(index_name) AS (
    VALUES
        ('idx_groups_org_id'),
        ('idx_group_members_user'),
        ('idx_notice_created_at'),
        ('idx_notice_important_created'),
        ('idx_notice_targets_notice'),
        ('idx_notice_targets_type_value'),
        ('idx_notice_receipts_user_delivered'),
        ('idx_chat_messages_room_created'),
        ('idx_chat_room_reads_room_user'),
        ('idx_chat_tasks_room_deadline'),
        ('idx_chat_tasks_assignee_status'),
        ('idx_surveys_status_created'),
        ('idx_survey_questions_survey_seq'),
        ('idx_survey_options_question_seq'),
        ('idx_survey_responses_survey'),
        ('idx_survey_answers_response')
)
SELECT e.index_name AS missing_index
FROM expected e
LEFT JOIN pg_indexes i
       ON i.schemaname = 'public'
      AND i.indexname = e.index_name
WHERE i.indexname IS NULL
ORDER BY e.index_name;

-- 5) Flyway 상태
SELECT
    CASE WHEN EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = 'flyway_schema_history'
    ) THEN 'OK' ELSE 'MISSING' END AS flyway_history_table;

SELECT installed_rank, version, description, type, success
FROM flyway_schema_history
ORDER BY installed_rank;
