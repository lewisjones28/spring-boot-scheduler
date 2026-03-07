CREATE TABLE IF NOT EXISTS job_configs (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'IDLE',
    next_run_time TIMESTAMP NOT NULL,
    last_run_time TIMESTAMP,
    interval_millis BIGINT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    modified_by VARCHAR(255)
);

CREATE INDEX idx_job_configs_name ON job_configs(name);
CREATE INDEX idx_job_configs_status ON job_configs(status);
CREATE INDEX idx_job_configs_next_run_time ON job_configs(next_run_time);
CREATE INDEX idx_job_configs_enabled ON job_configs(enabled);
CREATE INDEX idx_job_configs_status_next_run_time ON job_configs(status, next_run_time);
CREATE INDEX idx_job_configs_enabled_status_next_run_time ON job_configs(enabled, status, next_run_time);