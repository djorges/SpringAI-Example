CREATE TABLE ai_responses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id BIGINT NOT NULL,
    provider VARCHAR(30) NOT NULL,
    content LONGTEXT,
    status VARCHAR(20) NOT NULL,
    response_time_ms BIGINT,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ai_response_message
        FOREIGN KEY (message_id)
        REFERENCES messages(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_message_provider
        UNIQUE (message_id, provider),

    INDEX idx_ai_provider (provider),
    INDEX idx_ai_status (status)
);
