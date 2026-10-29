package com.orudesk.orudesk.patients.adapter.in.web;

import com.orudesk.orudesk.identity.CurrentOrgUnit;
import com.orudesk.orudesk.patients.application.port.in.EditPatientCommand;
import com.orudesk.orudesk.patients.application.port.in.EditPatientUseCase;
import com.orudesk.orudesk.patients.application.port.in.GetPatientQuery;
import com.orudesk.orudesk.patients.application.port.in.PatientDetails;
import com.orudesk.orudesk.patients.application.port.in.PatientRegistered;
import com.orudesk.orudesk.patients.application.port.in.RegisterPatientCommand;
import com.orudesk.orudesk.patients.application.port.in.RegisterPatientUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

/** Who may do what is decided in the service (UnitAccess), as in the school edition. */
@RestController
@RequestMapping("/api/v1/patients")
@Tag(name = "Patients")
class PatientController {

    private final RegisterPatientUseCase registerPatient;
    private final EditPatientUseCase editPatient;
    private final GetPatientQuery getPatient;
    private final CurrentOrgUnit currentOrgUnit;

    PatientController(RegisterPatientUseCase registerPatient, EditPatientUseCase editPatient, GetPatientQuery getPatient,
                      CurrentOrgUnit currentOrgUnit) {
        this.registerPatient = registerPatient;
        this.editPatient = editPatient;
        this.getPatient = getPatient;
        this.currentOrgUnit = currentOrgUnit;
    }

    @PostMapping
    @ApiResponse(responseCode = "201", description = "Registered", useReturnTypeSchema = true)
    @Operation(operationId = "registerPatient", summary = "Register a patient at the hospital or clinic in X-Org-Unit",
            parameters = @Parameter(name = CurrentOrgUnit.HEADER, in = ParameterIn.HEADER, required = true,
                    description = "The hospital or clinic", schema = @Schema(type = "string", format = "uuid")))
    ResponseEntity<PatientRegistered> register(@Valid @RequestBody RegisterPatientRequest request) {
        PatientRegistered registered = registerPatient.register(currentOrgUnit.require(), new RegisterPatientCommand(
                request.firstName(), request.lastName(), request.dateOfBirth(), request.phone(), request.email(),
                request.bloodGroup(), request.emergencyContactName(), request.emergencyContactPhone()));
        return ResponseEntity.created(URI.create("/api/v1/patients/" + registered.id())).body(registered);
    }

    /** GET /api/v1/patients?mrn=MR0000001 : the front desk's lookup by the number on the card. */
    @GetMapping
    @Operation(operationId = "findPatientByMrn", summary = "The patient with this medical record number")
    PatientDetails findByMrn(@RequestParam String mrn) {
        return getPatient.findByMrn(mrn);
    }

    @GetMapping("/{patientId}")
    @Operation(operationId = "getPatient", summary = "One patient")
    PatientDetails get(@PathVariable UUID patientId) {
        return getPatient.getPatient(patientId);
    }

    /** Anyone who may look patients up may correct their details: the front desk at any of the group's units. */
    @PutMapping("/{patientId}")
    @Operation(operationId = "editPatient", summary = "Change a patient's details",
            description = "Send both versions you read. 409 if the patient or the person was changed since: reload and edit again.")
    PatientDetails edit(@PathVariable UUID patientId, @Valid @RequestBody EditPatientRequest request) {
        return editPatient.edit(new EditPatientCommand(patientId, request.version(), request.personVersion(),
                request.firstName(), request.lastName(), request.dateOfBirth(), request.phone(), request.email(),
                request.bloodGroup(), request.emergencyContactName(), request.emergencyContactPhone()));
    }
}
