CREATE TABLE IF NOT EXISTS job_executions (
    id BIGSERIAL PRIMARY KEY,
    job_id INTEGER NOT NULL REFERENCES job_configs(id),
    status VARCHAR(50) NOT NULL,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    error_message TEXT,
    error_stacktrace TEXT,
    executed_by VARCHAR(255)
);

CREATE INDEX idx_job_execution_job_id ON job_executions(job_id);
CREATE INDEX idx_job_execution_status ON job_executions(status);
CREATE INDEX idx_job_execution_started_at ON job_executions(started_at);
CREATE INDEX idx_job_execution_job_id_status ON job_executions(job_id, status);