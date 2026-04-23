CREATE TABLE expenses (
    id UUID PRIMARY KEY,
    amount NUMERIC(19,4) NOT NULL CHECK (amount > 0),
    category VARCHAR(64) NOT NULL,
    description VARCHAR(500) NOT NULL,
    date DATE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    idempotency_key VARCHAR(64) NOT NULL UNIQUE,
    request_hash VARCHAR(64) NOT NULL
);
