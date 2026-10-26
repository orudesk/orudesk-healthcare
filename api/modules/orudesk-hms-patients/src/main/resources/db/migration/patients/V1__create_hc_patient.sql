-- A patient is a person from the People registry with a medical record number (MRN), registered at one of
-- the tenant's hospitals or clinics (an org unit). The MRN is per tenant: one record for the whole group,
-- whichever of its hospitals the patient walks into.
CREATE TABLE hc_patient (
    id                       UUID          NOT NULL,
    tenant_id                UUID          NOT NULL,
    person_id                UUID          NOT NULL,
    mrn                      VARCHAR(20)   NOT NULL,
    registered_at_unit_id    UUID          NOT NULL,     -- where they were first registered
    blood_group              VARCHAR(7)    NULL,         -- unknown until tested
    emergency_contact_name   VARCHAR(200)  NULL,
    emergency_contact_phone  VARCHAR(30)   NULL,
    registered_on            DATE          NOT NULL,
    created_at               TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_hc_patient_tenant FOREIGN KEY (tenant_id) REFERENCES iam_tenant (id),
    CONSTRAINT fk_hc_patient_person FOREIGN KEY (person_id) REFERENCES party_person (id) ON DELETE RESTRICT,
    CONSTRAINT fk_hc_patient_unit FOREIGN KEY (tenant_id, registered_at_unit_id) REFERENCES iam_org_unit (tenant_id, id),
    CONSTRAINT uk_hc_patient_person UNIQUE (tenant_id, person_id),
    -- The last line of defence for "no two patients share an MRN", whatever generates it
    CONSTRAINT uk_hc_patient_mrn UNIQUE (tenant_id, mrn),
    CONSTRAINT ck_hc_patient_blood_group
        CHECK (blood_group IN ('A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-')),
    -- An emergency contact is a name and a number, or nothing
    CONSTRAINT ck_hc_patient_emergency_contact
        CHECK ((emergency_contact_name IS NULL) = (emergency_contact_phone IS NULL))
);

-- The MRN counter: one row per tenant, holding the last number handed out. Getting the next number is one
-- statement (see MrnCounterAdapter), which locks this tenant's row until the transaction ends: two
-- registrations at the same moment queue for a few milliseconds instead of getting the same number.
CREATE TABLE hc_mrn_counter (
    tenant_id   UUID    NOT NULL,
    last_value  BIGINT  NOT NULL,
    PRIMARY KEY (tenant_id),
    CONSTRAINT fk_hc_mrn_counter_tenant FOREIGN KEY (tenant_id) REFERENCES iam_tenant (id)
);
