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
