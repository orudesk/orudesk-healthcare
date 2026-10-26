package com.orudesk.orudesk.patients.application.port.in;

import java.util.UUID;

/**
 * A patient record is the group's, not one hospital's: anyone who may register patients somewhere in the
 * tenant may look a patient up (the patient registered at the clinic walks into the hospital).
 */
public interface GetPatientQuery {

    PatientDetails getPatient(UUID patientId);

    /** The front desk types or scans the MRN from the patient's card. */
    PatientDetails findByMrn(String mrn);
}
