package com.orudesk.orudesk.patients.application.port.in;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.UUID;

/**
 * A patient as the front desk sees them: the person's details from the registry and the hospital's own.
 *
 * @param version       the patient record's version, for an edit (Day 49)
 * @param personVersion the person's version in the registry: the name and phone live there, so an edit names both
 */
public record PatientDetails(UUID id, String mrn, UUID personId, String firstName, String lastName,
                             @Nullable LocalDate dateOfBirth, @Nullable String phone, @Nullable String email,
                             @Nullable String bloodGroup, @Nullable String emergencyContactName,
                             @Nullable String emergencyContactPhone, UUID registeredAtUnitId, LocalDate registeredOn,
                             long version, long personVersion) {
}
