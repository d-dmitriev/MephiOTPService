-- Users
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    login VARCHAR(50) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    role VARCHAR(10) CHECK (role IN ('ADMIN', 'USER')) NOT NULL
);

-- OTP Config
CREATE TABLE IF NOT EXISTS otp_config (
    id SERIAL PRIMARY KEY,
    code_length INT NOT NULL DEFAULT 6,
    expiration_time INT NOT NULL DEFAULT 300 -- in seconds
);

-- Insert default config if empty
INSERT INTO otp_config (code_length, expiration_time)
SELECT 6, 300
WHERE NOT EXISTS (SELECT 1 FROM otp_config);

-- OTP Codes
CREATE TABLE IF NOT EXISTS otp_codes (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id),
    operation_id VARCHAR(100),
    code TEXT NOT NULL,
    status VARCHAR(10) CHECK (status IN ('ACTIVE', 'EXPIRED', 'USED')) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP
);