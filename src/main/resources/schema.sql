CREATE TABLE IF NOT EXISTS chat_messages (
    id          VARCHAR(36) PRIMARY KEY,
    room_id     VARCHAR(50)  NOT NULL,
    sender_id   VARCHAR(36)  NOT NULL,
    sender_name VARCHAR(100) NOT NULL,
    content     TEXT         NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_chat_messages_room_created
    ON chat_messages(room_id, created_at DESC);

CREATE TABLE IF NOT EXISTS chat_room_reads (
    user_id      VARCHAR(36) NOT NULL,
    room_id      VARCHAR(50) NOT NULL,
    last_read_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, room_id)
);

CREATE INDEX IF NOT EXISTS idx_chat_room_reads_room_user
    ON chat_room_reads(room_id, user_id);

CREATE TABLE IF NOT EXISTS chat_tasks (
    id                 VARCHAR(36) PRIMARY KEY,
    room_id            VARCHAR(50)  NOT NULL,
    title              VARCHAR(120) NOT NULL,
    description        TEXT,
    deadline_at        TIMESTAMP    NOT NULL,
    status             VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_by_user_id VARCHAR(36)  NOT NULL,
    created_by_username VARCHAR(50) NOT NULL,
    created_by_name    VARCHAR(100) NOT NULL,
    assignee_user_id   VARCHAR(36)  NOT NULL,
    assignee_username  VARCHAR(50)  NOT NULL,
    assignee_name      VARCHAR(100) NOT NULL,
    created_at         TIMESTAMP    NOT NULL DEFAULT NOW(),
    completed_at       TIMESTAMP,
    confirmed_at       TIMESTAMP
);

ALTER TABLE IF EXISTS chat_tasks
    ADD COLUMN IF NOT EXISTS created_by_username VARCHAR(50);
ALTER TABLE IF EXISTS chat_tasks
    ADD COLUMN IF NOT EXISTS confirmed_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_chat_tasks_room_deadline
    ON chat_tasks(room_id, deadline_at ASC);

CREATE INDEX IF NOT EXISTS idx_chat_tasks_assignee_status
    ON chat_tasks(assignee_user_id, status, deadline_at ASC);

ALTER TABLE IF EXISTS users
    ADD COLUMN IF NOT EXISTS can_post_notice BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE IF EXISTS users
    ADD COLUMN IF NOT EXISTS can_post_notification BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS surveys (
    id                  VARCHAR(36) PRIMARY KEY,
    title               VARCHAR(200) NOT NULL,
    description         TEXT,
    status              VARCHAR(20)  NOT NULL DEFAULT 'PUBLISHED',
    start_at            TIMESTAMP,
    end_at              TIMESTAMP,
    created_by_user_id  VARCHAR(36)  NOT NULL,
    created_by_username VARCHAR(50)  NOT NULL,
    created_by_name     VARCHAR(100) NOT NULL,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_surveys_status_created
    ON surveys(status, created_at DESC);

CREATE TABLE IF NOT EXISTS survey_questions (
    id          VARCHAR(36) PRIMARY KEY,
    survey_id   VARCHAR(36)  NOT NULL,
    seq         INTEGER      NOT NULL,
    type        VARCHAR(20)  NOT NULL,
    title       VARCHAR(500) NOT NULL,
    is_required BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_survey_questions_survey_seq
    ON survey_questions(survey_id, seq);

CREATE TABLE IF NOT EXISTS survey_options (
    id           VARCHAR(36) PRIMARY KEY,
    question_id  VARCHAR(36)  NOT NULL,
    seq          INTEGER      NOT NULL,
    option_label VARCHAR(300) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_survey_options_question_seq
    ON survey_options(question_id, seq);

CREATE TABLE IF NOT EXISTS survey_responses (
    id           VARCHAR(36) PRIMARY KEY,
    survey_id    VARCHAR(36) NOT NULL,
    user_id      VARCHAR(36) NOT NULL,
    username     VARCHAR(50) NOT NULL,
    user_name    VARCHAR(100) NOT NULL,
    submitted_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    UNIQUE (survey_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_survey_responses_survey
    ON survey_responses(survey_id, submitted_at DESC);

CREATE TABLE IF NOT EXISTS survey_answers (
    id          VARCHAR(36) PRIMARY KEY,
    response_id VARCHAR(36) NOT NULL,
    survey_id   VARCHAR(36) NOT NULL,
    question_id VARCHAR(36) NOT NULL,
    option_id   VARCHAR(36),
    answer_text TEXT,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_survey_answers_response
    ON survey_answers(response_id);
