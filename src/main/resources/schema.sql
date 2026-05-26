-- Enable the uuid-ossp extension for UUID generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    firstName TEXT,
    lastName TEXT,
    email TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    address TEXT,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
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
    status TEXT DEFAULT 'PENDING' CHECK ( status IN ('PENDING', 'ACCEPTED', 'COMPLETED', 'IN_PROGRESS', 'CANCELLED', 'PENDING_CONFIRMATION', 'DISPUTED') ),
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    scheduled TIMESTAMP,
    FOREIGN KEY (providerId) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (clientId) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS task_durations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    taskId UUID NOT NULL,
    startTime TIMESTAMP,
    endTime TIMESTAMP,
    FOREIGN KEY (taskId) REFERENCES tasks(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS task_locations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    taskId UUID NOT NULL,
    address TEXT NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    FOREIGN KEY (taskId) REFERENCES tasks(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS task_tags (
    taskId UUID NOT NULL,
    tag TEXT NOT NULL,
    PRIMARY KEY (taskId, tag),
    FOREIGN KEY (taskId) REFERENCES tasks(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS task_images (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    taskId UUID NOT NULL,
    imageUrl TEXT NOT NULL,
    FOREIGN KEY (taskId) REFERENCES tasks(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS task_categories (
    taskId UUID NOT NULL,
    categoryId UUID NOT NULL,
    PRIMARY KEY (taskId, categoryId),
    FOREIGN KEY (taskId) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (categoryId) REFERENCES categories(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS offers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    taskId UUID NOT NULL,
    providerId UUID NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (taskId) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (providerId) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    taskId UUID NOT NULL,
    senderId UUID NOT NULL,
    content TEXT NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (taskId) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (senderId) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reviews (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    taskId UUID NOT NULL,
    reviewerId UUID NOT NULL,
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (taskId) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewerId) REFERENCES users(id) ON DELETE CASCADE
);