package com.orudesk.orudesk.patients.application.port.in;

import com.orudesk.orudesk.shared.AuditableEvent;

import java.util.UUID;

/**
 * Someone opened a patient's record (Day 69). Health records need an answer to "who looked at mine?", not only
 * "who changed it?": most misuse of medical records is reading, by staff who had no reason to.
 */
public record PatientViewed(UUID patientId, String mrn) implements AuditableEvent {

    @Override public String action() { return "patients.patient.viewed"; }
    @Override public String subjectType() { return "patient"; }
    @Override public UUID subjectId() { return patientId; }
    @Override public String summary() { return "Opened patient " + mrn; }
}
