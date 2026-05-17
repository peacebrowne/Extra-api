-- Enable the uuid-ossp extension for UUID generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    firstName TEXT,
    lastName TEXT,
    email TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    msisdn TEXT,
    role TEXT CHECK ( role IN ('POSTER', 'WORKER') ),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tasks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    poster_id UUID NOT NULL,
    worker_id UUID,
    title TEXT NOT NULL,
    description TEXT,
    budget DECIMAL,
    status TEXT DEFAULT 'OPEN' CHECK ( status IN ('OPEN', 'ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') ),
    posted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (worker_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (poster_id) REFERENCES users(id) ON DELETE CASCADE
)