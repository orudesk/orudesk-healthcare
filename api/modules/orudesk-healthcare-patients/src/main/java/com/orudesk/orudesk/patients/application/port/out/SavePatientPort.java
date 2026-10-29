package com.orudesk.orudesk.patients.application.port.out;

import com.orudesk.orudesk.patients.domain.Patient;

public interface SavePatientPort {

    void save(Patient patient);

    /** Writes new details if the stored row is still at patient.version(); returns the version after the save. */
    long update(Patient patient);
}
