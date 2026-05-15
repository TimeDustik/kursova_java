-- Hibernate 6 sends enum params as character varying; PostgreSQL custom enum
-- types have no implicit cast from varchar and operator overloading is also
-- unreliable. Converting all enum columns to TEXT (with CHECK constraints)
-- is the clean solution — Hibernate's default string binding works transparently.

-- Convert columns from custom enum type to TEXT (while types still exist)
ALTER TABLE users
    ALTER COLUMN role TYPE TEXT USING role::TEXT;

ALTER TABLE equipment
    ALTER COLUMN type   TYPE TEXT USING type::TEXT,
    ALTER COLUMN status TYPE TEXT USING status::TEXT;

ALTER TABLE licenses
    ALTER COLUMN license_type TYPE TEXT USING license_type::TEXT;

ALTER TABLE audit_logs
    ALTER COLUMN action TYPE TEXT USING action::TEXT;

ALTER TABLE notifications
    ALTER COLUMN type   TYPE TEXT USING type::TEXT,
    ALTER COLUMN status TYPE TEXT USING status::TEXT;

-- Drop custom enum types (CASCADE removes V3 implicit casts automatically)
DROP TYPE IF EXISTS user_role           CASCADE;
DROP TYPE IF EXISTS equipment_type      CASCADE;
DROP TYPE IF EXISTS equipment_status    CASCADE;
DROP TYPE IF EXISTS license_type        CASCADE;
DROP TYPE IF EXISTS audit_action        CASCADE;
DROP TYPE IF EXISTS notification_type   CASCADE;
DROP TYPE IF EXISTS notification_status CASCADE;

-- Add CHECK constraints to preserve value-level validation
ALTER TABLE users
    ADD CONSTRAINT chk_users_role
    CHECK (role IN ('WORKER','TEAM_LEAD','ADMIN','AUDITOR'));

ALTER TABLE equipment
    ADD CONSTRAINT chk_equipment_type
    CHECK (type IN ('LAPTOP','DESKTOP','MONITOR','PHONE','PRINTER','NETWORK','OTHER')),
    ADD CONSTRAINT chk_equipment_status
    CHECK (status IN ('IN_STOCK','ASSIGNED','DECOMMISSIONED'));

ALTER TABLE licenses
    ADD CONSTRAINT chk_licenses_license_type
    CHECK (license_type IN ('PERPETUAL','SUBSCRIPTION','OEM','VOLUME'));

ALTER TABLE audit_logs
    ADD CONSTRAINT chk_audit_logs_action
    CHECK (action IN ('CREATE','UPDATE','DELETE','ASSIGN','LOGIN','LOGOUT'));

ALTER TABLE notifications
    ADD CONSTRAINT chk_notifications_type
    CHECK (type IN ('LICENSE_EXPIRING','WARRANTY_EXPIRING','ASSET_ASSIGNED')),
    ADD CONSTRAINT chk_notifications_status
    CHECK (status IN ('PENDING','SENT','FAILED'));
