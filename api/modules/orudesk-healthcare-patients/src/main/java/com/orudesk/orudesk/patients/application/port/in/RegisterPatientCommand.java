package com.orudesk.orudesk.patients.application.port.in;

import java.time.LocalDate;

/**
 * A new patient: the person's details for the registry, and the hospital's first basics.
 *
 * @param bloodGroup as on a lab report ("O+"), or null if not known yet
 */
public record RegisterPatientCommand(String firstName, String lastName, LocalDate dateOfBirth, String phone,
                                     String email, String bloodGroup, String emergencyContactName,
                                     String emergencyContactPhone) {
}
