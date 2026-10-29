-- Day 49: a patient's hospital details (blood group, emergency contact) can be edited, with optimistic locking
-- like the core's people (party V3): an edit names the version it was made on.
ALTER TABLE hc_patient ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
