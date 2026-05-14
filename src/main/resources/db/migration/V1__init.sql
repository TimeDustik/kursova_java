-- ============================================================
-- V1__init.sql — Початкова схема бази даних
-- Система управління інвентарем для ІТ-департаменту
-- ============================================================

-- UUID generation extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- ENUM TYPES
-- ============================================================

CREATE TYPE user_role AS ENUM (
    'WORKER',
    'TEAM_LEAD',
    'ADMIN',
    'AUDITOR'
);

CREATE TYPE equipment_type AS ENUM (
    'LAPTOP',
    'DESKTOP',
    'MONITOR',
    'PHONE',
    'PRINTER',
    'NETWORK',
    'OTHER'
);

CREATE TYPE equipment_status AS ENUM (
    'IN_STOCK',
    'ASSIGNED',
    'IN_REPAIR',
    'DECOMMISSIONED'
);

CREATE TYPE license_type AS ENUM (
    'PERPETUAL',
    'SUBSCRIPTION',
    'OEM',
    'VOLUME'
);

CREATE TYPE audit_action AS ENUM (
    'CREATE',
    'UPDATE',
    'DELETE',
    'ASSIGN',
    'LOGIN',
    'LOGOUT'
);

CREATE TYPE notification_type AS ENUM (
    'LICENSE_EXPIRING',
    'WARRANTY_EXPIRING',
    'ASSET_ASSIGNED'
);

CREATE TYPE notification_status AS ENUM (
    'PENDING',
    'SENT',
    'FAILED'
);

-- ============================================================
-- SITES (must precede users because of FK cycle)
-- ============================================================

CREATE TABLE sites (
    id           UUID        NOT NULL DEFAULT uuid_generate_v4() PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    address      VARCHAR(500),
    city         VARCHAR(100),
    team_lead_id UUID,                           -- FK added after users table
    created_at   TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- ============================================================
-- USERS
-- ============================================================

CREATE TABLE users (
    id            UUID         NOT NULL DEFAULT uuid_generate_v4() PRIMARY KEY,
    username      VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(255),
    phone         VARCHAR(50),
    role          user_role    NOT NULL,
    site_id       UUID         REFERENCES sites(id) ON DELETE SET NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email    UNIQUE (email)
);

-- Resolve FK cycle: sites.team_lead_id → users
ALTER TABLE sites
    ADD CONSTRAINT fk_sites_team_lead
        FOREIGN KEY (team_lead_id) REFERENCES users(id) ON DELETE SET NULL;

-- ============================================================
-- EQUIPMENT
-- ============================================================

-- Auto-incrementing sequence for inventory numbers
CREATE SEQUENCE equipment_seq START 1 INCREMENT 1 NO MAXVALUE;

CREATE TABLE equipment (
    id               UUID             NOT NULL DEFAULT uuid_generate_v4() PRIMARY KEY,
    inventory_number VARCHAR(50)      NOT NULL,
    name             VARCHAR(255)     NOT NULL,
    type             equipment_type   NOT NULL,
    manufacturer     VARCHAR(255),
    model            VARCHAR(255),
    serial_number    VARCHAR(255),
    status           equipment_status NOT NULL DEFAULT 'IN_STOCK',
    purchase_date    DATE,
    warranty_until   DATE,
    price            NUMERIC(12, 2),
    site_id          UUID             NOT NULL REFERENCES sites(id),
    assigned_user_id UUID             REFERENCES users(id) ON DELETE SET NULL,
    notes            TEXT,
    created_at       TIMESTAMP        NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP        NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_equipment_inventory_number UNIQUE (inventory_number),
    CONSTRAINT uq_equipment_serial_number    UNIQUE (serial_number)
);

-- ============================================================
-- LICENSES
-- ============================================================

CREATE TABLE licenses (
    id               UUID         NOT NULL DEFAULT uuid_generate_v4() PRIMARY KEY,
    software_name    VARCHAR(255) NOT NULL,
    vendor           VARCHAR(255),
    license_key      TEXT         NOT NULL,           -- AES-encrypted via AttributeConverter
    license_type     license_type NOT NULL,
    seats_total      INTEGER      NOT NULL DEFAULT 1,
    seats_used       INTEGER      NOT NULL DEFAULT 0,
    purchase_date    DATE,
    expiration_date  DATE,                            -- NULL for PERPETUAL
    cost             NUMERIC(12, 2),
    site_id          UUID         NOT NULL REFERENCES sites(id),
    assigned_user_id UUID         REFERENCES users(id) ON DELETE SET NULL,
    notes            TEXT,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_licenses_seats CHECK (seats_used <= seats_total),
    CONSTRAINT ck_licenses_seats_non_negative CHECK (seats_used >= 0 AND seats_total >= 0)
);

-- ============================================================
-- AUDIT LOGS
-- ============================================================

CREATE TABLE audit_logs (
    id            UUID         NOT NULL DEFAULT uuid_generate_v4() PRIMARY KEY,
    actor_user_id UUID         REFERENCES users(id) ON DELETE SET NULL,
    action        audit_action NOT NULL,
    entity_type   VARCHAR(100) NOT NULL,
    entity_id     VARCHAR(255),
    payload       JSONB,
    ip_address    VARCHAR(50),
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ============================================================
-- REFRESH TOKENS
-- ============================================================

CREATE TABLE refresh_tokens (
    id         UUID      NOT NULL DEFAULT uuid_generate_v4() PRIMARY KEY,
    user_id    UUID      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token      VARCHAR(500) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked    BOOLEAN   NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_refresh_tokens_token UNIQUE (token)
);

-- ============================================================
-- NOTIFICATIONS
-- ============================================================

CREATE TABLE notifications (
    id                 UUID                NOT NULL DEFAULT uuid_generate_v4() PRIMARY KEY,
    recipient_user_id  UUID                NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type               notification_type   NOT NULL,
    payload            JSONB,
    status             notification_status NOT NULL DEFAULT 'PENDING',
    created_at         TIMESTAMP           NOT NULL DEFAULT NOW(),
    sent_at            TIMESTAMP
);

-- ============================================================
-- INDEXES
-- ============================================================

-- Users
CREATE INDEX idx_users_site_id  ON users(site_id);
CREATE INDEX idx_users_role     ON users(role);
CREATE INDEX idx_users_active   ON users(active);

-- Equipment
CREATE INDEX idx_equipment_site_id         ON equipment(site_id);
CREATE INDEX idx_equipment_assigned_user   ON equipment(assigned_user_id);
CREATE INDEX idx_equipment_status          ON equipment(status);
CREATE INDEX idx_equipment_type            ON equipment(type);
CREATE INDEX idx_equipment_warranty_until  ON equipment(warranty_until);

-- Licenses
CREATE INDEX idx_licenses_site_id          ON licenses(site_id);
CREATE INDEX idx_licenses_assigned_user    ON licenses(assigned_user_id);
CREATE INDEX idx_licenses_expiration_date  ON licenses(expiration_date);
CREATE INDEX idx_licenses_type             ON licenses(license_type);

-- Audit logs
CREATE INDEX idx_audit_logs_actor      ON audit_logs(actor_user_id);
CREATE INDEX idx_audit_logs_action     ON audit_logs(action);
CREATE INDEX idx_audit_logs_entity     ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);

-- Refresh tokens
CREATE INDEX idx_refresh_tokens_user_id    ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);

-- Notifications
CREATE INDEX idx_notifications_recipient ON notifications(recipient_user_id);
CREATE INDEX idx_notifications_status    ON notifications(status);
CREATE INDEX idx_notifications_type      ON notifications(type);
