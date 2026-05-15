-- Hibernate 6 sends enum parameters as character varying.
-- PostgreSQL custom enum types have no implicit cast from varchar,
-- so we register implicit casts for all domain enums.
CREATE CAST (character varying AS user_role)          WITH INOUT AS IMPLICIT;
CREATE CAST (character varying AS equipment_type)     WITH INOUT AS IMPLICIT;
CREATE CAST (character varying AS equipment_status)   WITH INOUT AS IMPLICIT;
CREATE CAST (character varying AS license_type)       WITH INOUT AS IMPLICIT;
CREATE CAST (character varying AS audit_action)       WITH INOUT AS IMPLICIT;
CREATE CAST (character varying AS notification_type)  WITH INOUT AS IMPLICIT;
CREATE CAST (character varying AS notification_status) WITH INOUT AS IMPLICIT;
