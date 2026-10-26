package com.orudesk.orudesk.patients.application.port.in;

import java.util.UUID;

public interface RegisterPatientUseCase {

    /**
     * Adds the person to the registry and registers them as a patient at this unit, with the tenant's next MRN.
     *
     * @throws com.orudesk.orudesk.patients.domain.NotAllowedAtUnitException without the right role at the unit
     */
    PatientRegistered register(UUID unitId, RegisterPatientCommand command);
}
