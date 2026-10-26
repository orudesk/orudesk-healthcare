package com.orudesk.orudesk.patients.domain;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/** A patient: a person from the registry, with an MRN, registered first at one unit of the tenant. */
public final class Patient {

    private final PatientId id;
    private final UUID personId;
    private final Mrn mrn;
    private final UUID registeredAtUnitId;
    private final BloodGroup bloodGroup;              // null: not known yet
    private final EmergencyContact emergencyContact;  // null: none given
    private final LocalDate registeredOn;

    private Patient(PatientId id, UUID personId, Mrn mrn, UUID registeredAtUnitId, BloodGroup bloodGroup,
                    EmergencyContact emergencyContact, LocalDate registeredOn) {
        this.id = Objects.requireNonNull(id);
        this.personId = Objects.requireNonNull(personId, "a patient is a person");
        this.mrn = Objects.requireNonNull(mrn, "a patient has an MRN");
        this.registeredAtUnitId = Objects.requireNonNull(registeredAtUnitId);
        this.bloodGroup = bloodGroup;
        this.emergencyContact = emergencyContact;
        this.registeredOn = Objects.requireNonNull(registeredOn);
    }

    public static Patient register(UUID personId, Mrn mrn, UUID unitId, BloodGroup bloodGroup,
                                   EmergencyContact emergencyContact, LocalDate today) {
        return new Patient(PatientId.newId(), personId, mrn, unitId, bloodGroup, emergencyContact, today);
    }

    public static Patient restore(PatientId id, UUID personId, Mrn mrn, UUID unitId, BloodGroup bloodGroup,
                                  EmergencyContact emergencyContact, LocalDate registeredOn) {
        return new Patient(id, personId, mrn, unitId, bloodGroup, emergencyContact, registeredOn);
    }

    public PatientId id() { return id; }
    public UUID personId() { return personId; }
    public Mrn mrn() { return mrn; }
    public UUID registeredAtUnitId() { return registeredAtUnitId; }
    public BloodGroup bloodGroup() { return bloodGroup; }
    public EmergencyContact emergencyContact() { return emergencyContact; }
    public LocalDate registeredOn() { return registeredOn; }
}
