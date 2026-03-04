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
