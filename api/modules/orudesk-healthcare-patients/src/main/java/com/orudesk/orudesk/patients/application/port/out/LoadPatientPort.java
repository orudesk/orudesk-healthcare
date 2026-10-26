package com.orudesk.orudesk.patients.application.port.out;

import com.orudesk.orudesk.patients.domain.Mrn;
import com.orudesk.orudesk.patients.domain.Patient;
import com.orudesk.orudesk.patients.domain.PatientId;

import java.util.Optional;

public interface LoadPatientPort {

    Optional<Patient> findById(PatientId id);

    Optional<Patient> findByMrn(Mrn mrn);
}
