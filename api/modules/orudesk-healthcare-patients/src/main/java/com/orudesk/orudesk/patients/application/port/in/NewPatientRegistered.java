package com.orudesk.orudesk.patients.application.port.in;

import com.orudesk.orudesk.shared.AuditableEvent;

import java.util.UUID;

/**
 * A patient was registered: recorded in the core's audit log (Day 42), with who did it. Not PatientRegistered,
 * which is the API's answer to the front desk (the id and the MRN).
 */
public record NewPatientRegistered(UUID patientId, String mrn, String fullName) implements AuditableEvent {

    @Override public String action() { return "patients.patient.registered"; }
    @Override public String subjectType() { return "patient"; }
    @Override public UUID subjectId() { return patientId; }
    @Override public String summary() { return "Registered patient " + mrn + ", " + fullName; }
}
