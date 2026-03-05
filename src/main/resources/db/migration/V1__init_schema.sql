CREATE TABLE IF NOT EXISTS users (
    id                    VARCHAR(36) PRIMARY KEY,
    username              VARCHAR(50)  NOT NULL UNIQUE,
    password              VARCHAR(255) NOT NULL,
    name                  VARCHAR(100) NOT NULL,
    role                  VARCHAR(30)  NOT NULL,
    created_at            TIMESTAMP    NOT NULL DEFAULT NOW(),
    enabled               BOOLEAN      NOT NULL DEFAULT TRUE,
    can_post_notice       BOOLEAN      NOT NULL DEFAULT FALSE,
    can_post_notification BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS groups (
    id         VARCHAR(36) PRIMARY KEY,
    org_id     VARCHAR(36),
    code       VARCHAR(50),
    name       VARCHAR(120) NOT NULL,
    type       VARCHAR(30),
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS group_members (
    group_id VARCHAR(36) NOT NULL,
    user_id  VARCHAR(36) NOT NULL,
    PRIMARY KEY (group_id, user_id)
);

CREATE TABLE IF NOT EXISTS notice (
    id           VARCHAR(36) PRIMARY KEY,
    title        VARCHAR(200) NOT NULL,
    content      TEXT         NOT NULL,
    author_id    VARCHAR(36)  NOT NULL,
    author_name  VARCHAR(100) NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    is_important BOOLEAN      NOT NULL DEFAULT FALSE,
    is_hidden    BOOLEAN      NOT NULL DEFAULT FALSE,
    view_count   INTEGER      NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS notice_targets (
    id           VARCHAR(36) PRIMARY KEY,
    notice_id    VARCHAR(36) NOT NULL,
    target_type  VARCHAR(20) NOT NULL,
    target_value VARCHAR(100) NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS notice_receipts (
    id           VARCHAR(36) PRIMARY KEY,
    notice_id    VARCHAR(36) NOT NULL,
    user_id      VARCHAR(36) NOT NULL,
    delivered_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    read_at      TIMESTAMP,
    CONSTRAINT uq_notice_receipts_notice_user UNIQUE (notice_id, user_id)
);

CREATE TABLE IF NOT EXISTS chat_messages (
    id          VARCHAR(36) PRIMARY KEY,
    room_id     VARCHAR(50)  NOT NULL,
    sender_id   VARCHAR(36)  NOT NULL,
    sender_name VARCHAR(100) NOT NULL,
    content     TEXT         NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS chat_room_reads (
    user_id      VARCHAR(36) NOT NULL,
    room_id      VARCHAR(50) NOT NULL,
    last_read_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, room_id)
);

CREATE TABLE IF NOT EXISTS chat_tasks (
    id                  VARCHAR(36) PRIMARY KEY,
    room_id             VARCHAR(50)  NOT NULL,
    title               VARCHAR(120) NOT NULL,
    description         TEXT,
    deadline_at         TIMESTAMP    NOT NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_by_user_id  VARCHAR(36)  NOT NULL,
    created_by_username VARCHAR(50),
    created_by_name     VARCHAR(100) NOT NULL,
    assignee_user_id    VARCHAR(36)  NOT NULL,
    assignee_username   VARCHAR(50)  NOT NULL,
    assignee_name       VARCHAR(100) NOT NULL,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    completed_at        TIMESTAMP,
    confirmed_at        TIMESTAMP
);

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

CREATE TABLE IF NOT EXISTS survey_questions (
    id          VARCHAR(36) PRIMARY KEY,
    survey_id   VARCHAR(36)  NOT NULL,
    seq         INTEGER      NOT NULL,
    type        VARCHAR(20)  NOT NULL,
    title       VARCHAR(500) NOT NULL,
    is_required BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS survey_options (
    id           VARCHAR(36) PRIMARY KEY,
    question_id  VARCHAR(36)  NOT NULL,
    seq          INTEGER      NOT NULL,
    option_label VARCHAR(300) NOT NULL
);

CREATE TABLE IF NOT EXISTS survey_responses (
    id           VARCHAR(36) PRIMARY KEY,
    survey_id    VARCHAR(36) NOT NULL,
    user_id      VARCHAR(36) NOT NULL,
    username     VARCHAR(50) NOT NULL,
    user_name    VARCHAR(100) NOT NULL,
    submitted_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_survey_responses_survey_user UNIQUE (survey_id, user_id)
);

CREATE TABLE IF NOT EXISTS survey_answers (
    id          VARCHAR(36) PRIMARY KEY,
    response_id VARCHAR(36) NOT NULL,
    survey_id   VARCHAR(36) NOT NULL,
    question_id VARCHAR(36) NOT NULL,
    option_id   VARCHAR(36),
    answer_text TEXT,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS can_post_notice BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS can_post_notification BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE users
SET can_post_notification = can_post_notice
WHERE can_post_notification = FALSE;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_group_members_group') THEN
        ALTER TABLE group_members
            ADD CONSTRAINT fk_group_members_group
                FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_group_members_user') THEN
        ALTER TABLE group_members
            ADD CONSTRAINT fk_group_members_user
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_notice_targets_notice') THEN
        ALTER TABLE notice_targets
            ADD CONSTRAINT fk_notice_targets_notice
                FOREIGN KEY (notice_id) REFERENCES notice(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_notice_receipts_notice') THEN
        ALTER TABLE notice_receipts
            ADD CONSTRAINT fk_notice_receipts_notice
                FOREIGN KEY (notice_id) REFERENCES notice(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_notice_receipts_user') THEN
        ALTER TABLE notice_receipts
            ADD CONSTRAINT fk_notice_receipts_user
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_chat_messages_sender') THEN
        ALTER TABLE chat_messages
            ADD CONSTRAINT fk_chat_messages_sender
                FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_chat_room_reads_user') THEN
        ALTER TABLE chat_room_reads
            ADD CONSTRAINT fk_chat_room_reads_user
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_chat_tasks_created_by_user') THEN
        ALTER TABLE chat_tasks
            ADD CONSTRAINT fk_chat_tasks_created_by_user
                FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE RESTRICT;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_chat_tasks_assignee_user') THEN
        ALTER TABLE chat_tasks
            ADD CONSTRAINT fk_chat_tasks_assignee_user
                FOREIGN KEY (assignee_user_id) REFERENCES users(id) ON DELETE RESTRICT;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_surveys_created_by_user') THEN
        ALTER TABLE surveys
            ADD CONSTRAINT fk_surveys_created_by_user
                FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE RESTRICT;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_survey_questions_survey') THEN
        ALTER TABLE survey_questions
            ADD CONSTRAINT fk_survey_questions_survey
                FOREIGN KEY (survey_id) REFERENCES surveys(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_survey_options_question') THEN
        ALTER TABLE survey_options
            ADD CONSTRAINT fk_survey_options_question
                FOREIGN KEY (question_id) REFERENCES survey_questions(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_survey_responses_survey') THEN
        ALTER TABLE survey_responses
            ADD CONSTRAINT fk_survey_responses_survey
                FOREIGN KEY (survey_id) REFERENCES surveys(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_survey_responses_user') THEN
        ALTER TABLE survey_responses
            ADD CONSTRAINT fk_survey_responses_user
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_survey_answers_response') THEN
        ALTER TABLE survey_answers
            ADD CONSTRAINT fk_survey_answers_response
                FOREIGN KEY (response_id) REFERENCES survey_responses(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_survey_answers_survey') THEN
        ALTER TABLE survey_answers
            ADD CONSTRAINT fk_survey_answers_survey
                FOREIGN KEY (survey_id) REFERENCES surveys(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_survey_answers_question') THEN
        ALTER TABLE survey_answers
            ADD CONSTRAINT fk_survey_answers_question
                FOREIGN KEY (question_id) REFERENCES survey_questions(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_survey_answers_option') THEN
        ALTER TABLE survey_answers
            ADD CONSTRAINT fk_survey_answers_option
                FOREIGN KEY (option_id) REFERENCES survey_options(id) ON DELETE SET NULL;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_groups_org_id ON groups(org_id);
CREATE INDEX IF NOT EXISTS idx_group_members_user ON group_members(user_id);

CREATE INDEX IF NOT EXISTS idx_notice_created_at ON notice(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notice_important_created ON notice(is_important DESC, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notice_targets_notice ON notice_targets(notice_id);
CREATE INDEX IF NOT EXISTS idx_notice_targets_type_value ON notice_targets(target_type, target_value);
CREATE INDEX IF NOT EXISTS idx_notice_receipts_user_delivered ON notice_receipts(user_id, delivered_at DESC);

CREATE INDEX IF NOT EXISTS idx_chat_messages_room_created ON chat_messages(room_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_chat_room_reads_room_user ON chat_room_reads(room_id, user_id);
CREATE INDEX IF NOT EXISTS idx_chat_tasks_room_deadline ON chat_tasks(room_id, deadline_at ASC);
CREATE INDEX IF NOT EXISTS idx_chat_tasks_assignee_status ON chat_tasks(assignee_user_id, status, deadline_at ASC);

CREATE INDEX IF NOT EXISTS idx_surveys_status_created ON surveys(status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_survey_questions_survey_seq ON survey_questions(survey_id, seq);
CREATE INDEX IF NOT EXISTS idx_survey_options_question_seq ON survey_options(question_id, seq);
CREATE INDEX IF NOT EXISTS idx_survey_responses_survey ON survey_responses(survey_id, submitted_at DESC);
CREATE INDEX IF NOT EXISTS idx_survey_answers_response ON survey_answers(response_id);
