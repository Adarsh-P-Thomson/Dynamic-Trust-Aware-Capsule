-- PostgreSQL Schema for Dynamic Trust-Aware Capsule (DTA) - Revised
-- This script implements a more modular and secure database design, separating
-- user roles, adding detailed capsule lifecycle statuses, and integrating a
-- JSON-based policy engine.

-- Best practice: Use a dedicated schema to encapsulate the application's tables.
CREATE SCHEMA IF NOT EXISTS dta_capsule;
SET search_path TO dta_capsule;

-- Drop existing tables in reverse order of dependency to ensure a clean slate.
DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS capsule_access_grants CASCADE;
DROP TABLE IF EXISTS capsule_details CASCADE;
DROP TABLE IF EXISTS clients CASCADE;
DROP TABLE IF EXISTS admins CASCADE;


--
-- Table: admins
--
-- Stores administrator accounts. Admins are privileged users responsible for
-- creating and managing capsules and granting access to clients.
--
CREATE TABLE IF NOT EXISTS admins (
    admin_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(255) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL, -- Storing password hashes, never plain text.
    email VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_login TIMESTAMPTZ,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

COMMENT ON TABLE admins IS 'Stores administrator accounts responsible for capsule creation and management.';


--
-- Table: clients
--
-- Stores client user accounts. Clients are consumers of the capsules;
-- they can only view/interact with capsules they have been granted access to.
--
CREATE TABLE IF NOT EXISTS clients (
    client_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(255) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL, -- Storing password hashes, never plain text.
    email VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_login TIMESTAMPTZ,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

COMMENT ON TABLE clients IS 'Stores client user accounts who can be granted access to capsules.';


--
-- Table: capsule_details
--
-- Stores the core metadata and security configuration for each capsule.
-- This table does NOT store the file's location, only its hash for integrity checks.
--
CREATE TABLE IF NOT EXISTS capsule_details (
    capsule_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    creator_admin_id UUID NOT NULL REFERENCES admins(admin_id),
    capsule_name VARCHAR(255) NOT NULL,
    description TEXT,
    -- The SHA-256 hash of the original, unencrypted file for integrity verification.
    file_hash_sha256 TEXT NOT NULL UNIQUE,
    -- The encryption key for the capsule, which itself should be encrypted
    -- with a master key or stored in a secure vault.
    encrypted_key BYTEA NOT NULL,
    -- Controls if the capsule can be accessed. 'locked' is the default secure state.
    -- It must be explicitly set to 'unlocked' for access to be possible.
    status VARCHAR(50) NOT NULL CHECK (status IN ('locked', 'unlocked')) DEFAULT 'locked',
    -- Manages the capsule's lifecycle, including destruction policy.
    lifecycle_status VARCHAR(50) NOT NULL CHECK (lifecycle_status IN ('active', 'expired', 'marked_for_destruction')) DEFAULT 'active',
    -- A flexible JSONB field to store the security manifest and access rules
    -- (e.g., OPA policies, allowed applications, geo-restrictions).
    policy JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ,
    -- Timestamp for when the capsule was marked for destruction.
    destruction_marked_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_capsule_details_creator_admin_id ON capsule_details(creator_admin_id);
CREATE INDEX IF NOT EXISTS idx_capsule_details_status ON capsule_details(status);
CREATE INDEX IF NOT EXISTS idx_capsule_details_lifecycle_status ON capsule_details(lifecycle_status);

COMMENT ON TABLE capsule_details IS 'Stores detailed metadata, status, and security policies for each capsule.';
COMMENT ON COLUMN capsule_details.file_hash_sha256 IS 'SHA-256 hash of the original file for integrity verification. Assumed to be unique.';
COMMENT ON COLUMN capsule_details.status IS 'Controls the immediate accessibility of the capsule (locked/unlocked).';
COMMENT ON COLUMN capsule_details.lifecycle_status IS 'Tracks the long-term state of the capsule (active, expired, marked_for_destruction).';
COMMENT ON COLUMN capsule_details.policy IS 'Stores a JSON object defining access rules, like allowed apps, geo-fencing, or OPA-style policies.';


--
-- Table: capsule_access_grants
--
-- This is the central transaction table that associates a client with a capsule,
-- explicitly recording which admin granted the access.
--
CREATE TABLE IF NOT EXISTS capsule_access_grants (
    grant_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    capsule_id UUID NOT NULL REFERENCES capsule_details(capsule_id) ON DELETE CASCADE,
    client_id UUID NOT NULL REFERENCES clients(client_id) ON DELETE CASCADE,
    granting_admin_id UUID NOT NULL REFERENCES admins(admin_id),
    granted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    -- Access can have its own expiry, which may be sooner than the capsule's expiry.
    access_expires_at TIMESTAMPTZ,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at TIMESTAMPTZ,
    -- Ensure a client is only granted access to a capsule once.
    UNIQUE (capsule_id, client_id)
);

CREATE INDEX IF NOT EXISTS idx_capsule_access_grants_capsule_id ON capsule_access_grants(capsule_id);
CREATE INDEX IF NOT EXISTS idx_capsule_access_grants_client_id ON capsule_access_grants(client_id);

COMMENT ON TABLE capsule_access_grants IS 'Transactional table linking clients to capsules, recording who granted the access.';


--
-- Table: audit_logs
--
-- Records all significant actions for security and compliance. It can now
-- distinguish between actions taken by admins and clients.
--
CREATE TABLE IF NOT EXISTS audit_logs (
    log_id BIGSERIAL PRIMARY KEY,
    -- Actor columns: one of these should be populated for any user-driven action.
    admin_id UUID REFERENCES admins(admin_id),
    client_id UUID REFERENCES clients(client_id),
    -- The capsule related to the event, if applicable.
    capsule_id UUID REFERENCES capsule_details(capsule_id),
    action_type VARCHAR(100) NOT NULL,
    client_ip_address INET,
    user_agent TEXT,
    log_timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    -- A JSONB field to store additional details about the event.
    details JSONB,
    -- Ensures that a log entry is associated with at most one type of user.
    CONSTRAINT chk_audit_actor CHECK ( (admin_id IS NULL AND client_id IS NOT NULL) OR (admin_id IS NOT NULL AND client_id IS NULL) OR (admin_id IS NULL AND client_id IS NULL) )
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_admin_id ON audit_logs(admin_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_client_id ON audit_logs(client_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_capsule_id ON audit_logs(capsule_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_action_type ON audit_logs(action_type);

COMMENT ON TABLE audit_logs IS 'Logs all significant events, distinguishing between admin and client actions.';
COMMENT ON COLUMN audit_logs.details IS 'Stores flexible, structured data about the event, like parameters or results.';
COMMENT ON CONSTRAINT chk_audit_actor ON audit_logs IS 'Ensures a log is tied to an admin, a client, or neither (system action), but not both.';


-- End of script
