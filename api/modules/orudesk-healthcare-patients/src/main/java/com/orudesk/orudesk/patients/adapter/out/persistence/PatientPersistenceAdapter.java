package com.orudesk.orudesk.patients.adapter.out.persistence;

import com.orudesk.orudesk.patients.application.port.out.LoadPatientPort;
import com.orudesk.orudesk.patients.application.port.out.SavePatientPort;
import com.orudesk.orudesk.patients.domain.BloodGroup;
import com.orudesk.orudesk.patients.domain.EmergencyContact;
import com.orudesk.orudesk.patients.domain.Mrn;
import com.orudesk.orudesk.patients.domain.Patient;
import com.orudesk.orudesk.patients.domain.PatientId;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class PatientPersistenceAdapter implements SavePatientPort, LoadPatientPort {

    private final PatientJpaRepository patients;

    PatientPersistenceAdapter(PatientJpaRepository patients) {
        this.patients = patients;
    }

    @Override
    public void save(Patient patient) {
        EmergencyContact contact = patient.emergencyContact();
        patients.save(new PatientJpaEntity(patient.id().value(), patient.personId(), patient.mrn().value(),
                patient.registeredAtUnitId(), patient.bloodGroup() == null ? null : patient.bloodGroup().label(),
                contact == null ? null : contact.name(), contact == null ? null : contact.phone(),
                patient.registeredOn()));
    }

    @Override
    public Optional<Patient> findById(PatientId id) {
        return patients.findById(id.value()).map(PatientPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<Patient> findByMrn(Mrn mrn) {
        return patients.findByMrn(mrn.value()).map(PatientPersistenceAdapter::toDomain);
    }

    private static Patient toDomain(PatientJpaEntity row) {
        return Patient.restore(new PatientId(row.getId()), row.getPersonId(), new Mrn(row.getMrn()),
                row.getRegisteredAtUnitId(),
                row.getBloodGroup() == null ? null : BloodGroup.fromLabel(row.getBloodGroup()),
                row.getEmergencyContactName() == null ? null
                        : new EmergencyContact(row.getEmergencyContactName(), row.getEmergencyContactPhone()),
                row.getRegisteredOn());
    }
}
