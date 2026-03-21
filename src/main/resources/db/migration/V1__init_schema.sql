CREATE TYPE ADMIN_ROLE AS ENUM ('SUPERADMIN', 'ADMIN');
CREATE TYPE LOBBY_DIFFICULTY AS ENUM ('EASY', 'MEDIUM', 'HARD');
CREATE TYPE LOBBY_STATUS AS ENUM ('ACTIVE', 'CLOSED');
CREATE TYPE SESSION_STATUS AS ENUM ('STARTED', 'COMPLETED', 'SUSPICIOUS');

CREATE TABLE admins (
    id uuid PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    position VARCHAR(255),
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ADMIN_ROLE NOT NULL DEFAULT 'ADMIN',
    created_at timestamp with time zone NOT NULL DEFAULT NOW(),
    updated_at timestamp with time zone NOT NULL DEFAULT NOW()
);

CREATE TABLE lobbies (
    id uuid PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    game VARCHAR(50) NOT NULL,
    difficulty LOBBY_DIFFICULTY,
    duration_minutes INTEGER NOT NULL,
    max_attempts INTEGER NOT NULL,
    game_over_text TEXT NOT NULL,
    status LOBBY_STATUS NOT NULL DEFAULT 'ACTIVE',
    invite_code VARCHAR(64) NOT NULL,
    created_at timestamp with time zone NOT NULL DEFAULT NOW(),
    closed_at timestamp with time zone
);

CREATE TABLE players (
    id uuid PRIMARY KEY,
    lobby_id BIGINT NOT NULL REFERENCES lobbies(id) ON DELETE CASCADE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(32) NOT NULL,
    email VARCHAR(255) NOT NULL,
    created_at timestamp with time zone NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_player_lobby_phone UNIQUE (lobby_id, phone),
    CONSTRAINT uq_player_lobby_email UNIQUE (lobby_id, email)
);

CREATE TABLE game_sessions (
    id BIGSERIAL PRIMARY KEY,
    player_id BIGINT NOT NULL REFERENCES players(id) ON DELETE CASCADE,
    lobby_id BIGINT NOT NULL REFERENCES lobbies(id) ON DELETE CASCADE,
    attempt_no INTEGER NOT NULL,
    session_token VARCHAR(128) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    finished_at TIMESTAMPTZ,
    duration_seconds INTEGER,
    final_score INTEGER,
    status SESSION_STATUS NOT NULL DEFAULT 'STARTED',
    CONSTRAINT uq_session_token UNIQUE (session_token),
    CONSTRAINT uq_player_attempt UNIQUE (player_id, attempt_no)
);

CREATE UNIQUE INDEX ux_admins_email_lower ON admins (LOWER(email));
CREATE UNIQUE INDEX ux_lobbies_invite_code ON lobbies (invite_code);
CREATE UNIQUE INDEX ux_lobbies_single_active ON lobbies (status) WHERE status = 'ACTIVE';

CREATE INDEX ix_lobbies_status_created_at ON lobbies (status, created_at DESC);

CREATE INDEX ix_players_lobby_name ON players (lobby_id, LOWER(last_name), LOWER(first_name));
CREATE INDEX ix_players_phone_lookup ON players (phone, lobby_id);

CREATE INDEX ix_sessions_lobby_results ON game_sessions (lobby_id, status, final_score DESC, duration_seconds ASC, finished_at DESC);
CREATE INDEX ix_sessions_player_started_at ON game_sessions (player_id, started_at DESC);
CREATE INDEX ix_sessions_status_finished_at ON game_sessions (status, finished_at DESC);
