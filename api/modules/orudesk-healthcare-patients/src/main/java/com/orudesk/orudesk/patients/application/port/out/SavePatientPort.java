package com.orudesk.orudesk.patients.application.port.out;

import com.orudesk.orudesk.patients.domain.Patient;

public interface SavePatientPort {

    void save(Patient patient);
}
