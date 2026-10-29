package com.orudesk.orudesk.patients.application.port.in;

import com.orudesk.orudesk.shared.AuditableEvent;
import com.orudesk.orudesk.shared.FieldChange;

import java.util.List;
import java.util.UUID;

/**
 * The hospital's details of a patient changed (Day 49). The person's own details (name, phone) are recorded by
 * the core as party.person.edited, on the person: each module audits what it owns.
 */
public record PatientEdited(UUID patientId, String mrn, List<FieldChange> changes) implements AuditableEvent {

    public PatientEdited {
        changes = List.copyOf(changes);
    }

    @Override public String action() { return "patients.patient.edited"; }
    @Override public String subjectType() { return "patient"; }
    @Override public UUID subjectId() { return patientId; }

    @Override
    public String summary() {
        return "Changed " + String.join(", ", changes.stream().map(FieldChange::field).toList()) + " of patient " + mrn;
    }
}
