CREATE TABLE job_application (
    id          BIGSERIAL PRIMARY KEY,
    company     VARCHAR(255)  NOT NULL,
    position    VARCHAR(255)  NOT NULL,
    job_url     VARCHAR(500),
    status      VARCHAR(20)   NOT NULL,
    notes       VARCHAR(2000),
    created_at  TIMESTAMPTZ   NOT NULL,
    updated_at  TIMESTAMPTZ   NOT NULL
);

CREATE INDEX idx_job_application_status  ON job_application (status);
CREATE INDEX idx_job_application_company ON job_application (LOWER(company));

CREATE TABLE status_change (
    id             BIGSERIAL PRIMARY KEY,
    application_id BIGINT      NOT NULL REFERENCES job_application (id) ON DELETE CASCADE,
    from_status    VARCHAR(20),
    to_status      VARCHAR(20) NOT NULL,
    changed_at     TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_status_change_application ON status_change (application_id);
