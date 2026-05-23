-- Enable the uuid-ossp extension for UUID generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    firstName TEXT,
    lastName TEXT,
    email TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    msisdn TEXT,
    role TEXT CHECK ( role IN ('CLIENT', 'PROVIDER') ),
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tasks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    clientId UUID NOT NULL,
    providerId UUID,
    title TEXT NOT NULL,
    description TEXT,
    status TEXT DEFAULT 'PENDING' CHECK ( status IN ('PENDING', 'ACCEPTED', 'COMPLETED', 'CANCELLED') ),
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    scheduled TIMESTAMP,
    FOREIGN KEY (providerId) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (clientId) REFERENCES users(id) ON DELETE CASCADE
)