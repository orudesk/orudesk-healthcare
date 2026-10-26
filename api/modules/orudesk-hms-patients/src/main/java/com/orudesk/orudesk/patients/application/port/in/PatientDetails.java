package com.orudesk.orudesk.patients.application.port.in;

import java.time.LocalDate;
import java.util.UUID;

public record PatientDetails(UUID id, String mrn, UUID personId, String firstName, String lastName,
                             LocalDate dateOfBirth, String phone, String bloodGroup, String emergencyContactName,
                             String emergencyContactPhone, UUID registeredAtUnitId, LocalDate registeredOn) {
}
