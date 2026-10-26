package com.orudesk.orudesk.patients.application.port.in;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.UUID;

public record PatientDetails(UUID id, String mrn, UUID personId, String firstName, String lastName,
                             @Nullable LocalDate dateOfBirth, @Nullable String phone, @Nullable String bloodGroup, @Nullable String emergencyContactName,
                             @Nullable String emergencyContactPhone, UUID registeredAtUnitId, LocalDate registeredOn) {
}
