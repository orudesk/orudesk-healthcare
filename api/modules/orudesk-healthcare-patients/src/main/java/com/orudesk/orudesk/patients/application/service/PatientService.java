package com.orudesk.orudesk.patients.application.service;

import com.orudesk.orudesk.party.application.port.in.EditPersonCommand;
import com.orudesk.orudesk.party.application.port.in.EditPersonUseCase;
import com.orudesk.orudesk.party.application.port.in.GetPersonQuery;
import com.orudesk.orudesk.party.application.port.in.PersonDetails;
import com.orudesk.orudesk.party.application.port.in.RegisterPersonCommand;
import com.orudesk.orudesk.party.application.port.in.RegisterPersonUseCase;
import com.orudesk.orudesk.patients.application.port.in.EditPatientCommand;
import com.orudesk.orudesk.patients.application.port.in.EditPatientUseCase;
import com.orudesk.orudesk.patients.application.port.in.GetPatientQuery;
import com.orudesk.orudesk.patients.application.port.in.PatientDetails;
import com.orudesk.orudesk.patients.application.port.in.PatientEdited;
import com.orudesk.orudesk.patients.application.port.in.NewPatientRegistered;
import com.orudesk.orudesk.patients.application.port.in.PatientRegistered;
import com.orudesk.orudesk.patients.application.port.in.RegisterPatientCommand;
import com.orudesk.orudesk.patients.application.port.in.RegisterPatientUseCase;
import com.orudesk.orudesk.patients.application.port.out.LoadPatientPort;
import com.orudesk.orudesk.patients.application.port.out.NextMrnPort;
import com.orudesk.orudesk.patients.application.port.out.SavePatientPort;
import com.orudesk.orudesk.patients.domain.BloodGroup;
import com.orudesk.orudesk.patients.domain.EmergencyContact;
import com.orudesk.orudesk.patients.domain.Mrn;
import com.orudesk.orudesk.patients.domain.NotAllowedAtUnitException;
import com.orudesk.orudesk.patients.domain.Patient;
import com.orudesk.orudesk.patients.domain.PatientId;
import com.orudesk.orudesk.patients.domain.PatientNotFoundException;
import com.orudesk.orudesk.shared.EditConflictException;
import com.orudesk.orudesk.shared.FieldChange;
import com.orudesk.orudesk.shared.FieldChanges;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
class PatientService implements RegisterPatientUseCase, EditPatientUseCase, GetPatientQuery {

    private final RegisterPersonUseCase registerPerson;
    private final EditPersonUseCase editPerson;
    private final GetPersonQuery getPerson;
    private final NextMrnPort nextMrnPort;
    private final SavePatientPort savePatientPort;
    private final LoadPatientPort loadPatientPort;
    private final UnitAccess unitAccess;
    private final Clock clock;
    private final ApplicationEventPublisher events;

    PatientService(RegisterPersonUseCase registerPerson, EditPersonUseCase editPerson, GetPersonQuery getPerson,
                   NextMrnPort nextMrnPort, SavePatientPort savePatientPort, LoadPatientPort loadPatientPort,
                   UnitAccess unitAccess, Clock clock, ApplicationEventPublisher events) {
        this.registerPerson = registerPerson;
        this.editPerson = editPerson;
        this.getPerson = getPerson;
        this.nextMrnPort = nextMrnPort;
        this.savePatientPort = savePatientPort;
        this.loadPatientPort = loadPatientPort;
        this.unitAccess = unitAccess;
        this.clock = clock;
        this.events = events;
    }

    @Override
    @Transactional
    public PatientRegistered register(UUID unitId, RegisterPatientCommand command) {
        if (!unitAccess.mayRegisterAt(unitId)) {
            throw new NotAllowedAtUnitException(unitId);
        }
        // Everything that can be wrong with the input fails here, before anything is written
        BloodGroup bloodGroup = bloodGroup(command.bloodGroup());
        EmergencyContact contact = emergencyContact(command.emergencyContactName(), command.emergencyContactPhone());

        UUID personId = registerPerson.register(new RegisterPersonCommand(
                command.firstName(), command.lastName(), command.dateOfBirth(), command.phone(), command.email()));

        // As late as possible: the counter row stays locked until this transaction commits, so the less
        // that happens after taking a number, the shorter other registrations of this tenant wait
        Mrn mrn = Mrn.fromNumber(nextMrnPort.next());
        Patient patient = Patient.register(personId, mrn, unitId, bloodGroup, contact, LocalDate.now(clock));
        savePatientPort.save(patient);
        events.publishEvent(new NewPatientRegistered(patient.id().value(), mrn.value(),
                command.firstName() + " " + command.lastName()));
        return new PatientRegistered(patient.id().value(), mrn.value());
    }

    /**
     * Two records in two modules, one transaction: the person in the registry (the core checks its version) and the
     * patient here (checked below). If either refuses, both roll back, so the front desk never saves half a form.
     */
    @Override
    @Transactional
    public PatientDetails edit(EditPatientCommand command) {
        PatientId id = new PatientId(command.patientId());
        Patient current = lookUp(loadPatientPort.findById(id), "id " + command.patientId());
        if (current.version() != command.version()) {
            throw new EditConflictException("patient", id.value(), command.version(), current.version());
        }
        Patient changed = current.changeDetails(bloodGroup(command.bloodGroup()),
                emergencyContact(command.emergencyContactName(), command.emergencyContactPhone()));

        editPerson.edit(new EditPersonCommand(current.personId(), command.personVersion(), command.firstName(),
                command.lastName(), command.dateOfBirth(), command.phone(), command.email()));

        List<FieldChange> changes = new FieldChanges()
                .compare("bloodGroup", label(current.bloodGroup()), label(changed.bloodGroup()))
                .compare("emergencyContactName", contactName(current), contactName(changed))
                .compare("emergencyContactPhone", contactPhone(current), contactPhone(changed))
                .list();
        if (!changes.isEmpty()) {
            savePatientPort.update(changed);
            events.publishEvent(new PatientEdited(id.value(), current.mrn().value(), changes));
        }
        return details(loadPatientPort.findById(id).orElseThrow());
    }

    private static BloodGroup bloodGroup(String label) {
        return label == null ? null : BloodGroup.fromLabel(label);
    }

    private static EmergencyContact emergencyContact(String name, String phone) {
        return name == null && phone == null ? null : new EmergencyContact(name, phone);
    }

    private static String label(BloodGroup bloodGroup) {
        return bloodGroup == null ? null : bloodGroup.label();
    }

    private static String contactName(Patient patient) {
        return patient.emergencyContact() == null ? null : patient.emergencyContact().name();
    }

    private static String contactPhone(Patient patient) {
        return patient.emergencyContact() == null ? null : patient.emergencyContact().phone();
    }

    @Override
    @Transactional(readOnly = true)
    public PatientDetails getPatient(UUID patientId) {
        PatientId id = new PatientId(patientId);
        return details(lookUp(loadPatientPort.findById(id), "id " + patientId));
    }

    @Override
    @Transactional(readOnly = true)
    public PatientDetails findByMrn(String mrn) {
        Mrn wanted = new Mrn(mrn);
        return details(lookUp(loadPatientPort.findByMrn(wanted), "MRN " + wanted.value()));
    }

    /** Someone without a patients role anywhere gets "not found", as if the patient didn't exist. */
    private Patient lookUp(Optional<Patient> found, String what) {
        return found.filter(_ -> unitAccess.mayLookUpPatients())
                .orElseThrow(() -> new PatientNotFoundException(what));
    }

    private PatientDetails details(Patient patient) {
        PersonDetails person = getPerson.getPerson(patient.personId());
        EmergencyContact contact = patient.emergencyContact();
        return new PatientDetails(patient.id().value(), patient.mrn().value(), person.id(), person.firstName(),
                person.lastName(), person.dateOfBirth(), person.phone(), person.email(), label(patient.bloodGroup()),
                contact == null ? null : contact.name(), contact == null ? null : contact.phone(),
                patient.registeredAtUnitId(), patient.registeredOn(), patient.version(), person.version());
    }
}
