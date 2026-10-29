package com.orudesk.orudesk.patients.application.port.in;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Everything on the patient's form, as it should now be (a replace, like PUT), with the two versions it was
 * read at: the patient's and the person's (PatientDetails.version and personVersion).
 */
public record EditPatientCommand(UUID patientId, long version, long personVersion, String firstName, String lastName,
                                 LocalDate dateOfBirth, String phone, String email, String bloodGroup,
                                 String emergencyContactName, String emergencyContactPhone) {
}
