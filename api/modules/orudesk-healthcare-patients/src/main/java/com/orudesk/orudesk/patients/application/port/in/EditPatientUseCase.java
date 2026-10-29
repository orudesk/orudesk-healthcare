package com.orudesk.orudesk.patients.application.port.in;

/**
 * Changes a patient's details: the person's in the registry and the hospital's own, in one transaction. Throws
 * the core's EditConflictException if either was changed since it was read, and PatientNotFoundException for
 * someone without a patients role (as for a lookup).
 */
public interface EditPatientUseCase {

    PatientDetails edit(EditPatientCommand command);
}
