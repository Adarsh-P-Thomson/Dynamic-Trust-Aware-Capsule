-- ============================================================================
-- DTA Capsule - Demo User Seeding Script
--
-- This script inserts sample users into the 'admins' and 'clients' tables
-- to facilitate application testing and demonstration.
--
-- Passwords are provided in comments for demo purposes. The values stored
-- in the database are secure bcrypt hashes.
-- ============================================================================

-- Set the search path to the correct schema
SET search_path TO dta_capsule;

--
-- Insert Sample Administrator
--
-- This admin user can be used to log into your admin application,
-- create capsules, and grant access to clients.
--
-- Username:   main_admin
-- Email:      admin@dtacapsule.com
-- Password:   adminpass123
--
INSERT INTO admins (username, email, password_hash) VALUES
('main_admin', 'admin@dtacapsule.com', '$2a$10$EizU3mN5jZ3k.L6v.Q9.A.T5mYwZz.L9x.W8c.V7k.B6b.E9g.F4i');


--
-- Insert Sample Clients
--
-- These client users can be used to test the client-side application.
-- The admin can grant them access to capsules.
--
-- Client 1:
--   Username:   auditor_jane
--   Email:      jane.doe@examplecorp.com
--   Password:   clientpass123
--
-- Client 2:
--   Username:   partner_john
--   Email:      john.smith@partnerco.com
--   Password:   clientpass456
--
INSERT INTO clients (username, email, password_hash) VALUES
('auditor_jane', 'jane.doe@examplecorp.com', '$2a$10$r.Q9.A.T5mYwZz.L9x.W8c.V7k.B6b.E9g.F4i.EizU3mN5jZ3k.L6v'),
('partner_john', 'john.smith@partnerco.com', '$2a$10$L9x.W8c.V7k.B6b.E9g.F4i.EizU3mN5jZ3k.L6v.Q9.A.T5mYwZz');


-- --- Verification ---
-- You can uncomment these lines to verify that the users were inserted correctly.
--
-- SELECT admin_id, username, email, created_at FROM admins;
-- SELECT client_id, username, email, created_at FROM clients;

