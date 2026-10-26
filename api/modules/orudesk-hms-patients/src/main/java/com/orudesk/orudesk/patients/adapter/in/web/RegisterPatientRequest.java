package com.orudesk.orudesk.patients.adapter.in.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** The JSON body of POST /api/v1/patients. The hospital or clinic comes from the X-Org-Unit header. */
record RegisterPatientRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        LocalDate dateOfBirth,
        @Size(max = 30) String phone,
        @Email @Size(max = 320) String email,
        @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "must be a blood group such as O+ or AB-") String bloodGroup,
        @Size(max = 200) String emergencyContactName,
        @Size(max = 30) String emergencyContactPhone) {
}
