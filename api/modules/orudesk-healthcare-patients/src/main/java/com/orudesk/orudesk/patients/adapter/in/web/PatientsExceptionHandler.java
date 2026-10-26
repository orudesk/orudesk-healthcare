package com.orudesk.orudesk.patients.adapter.in.web;

import com.orudesk.orudesk.patients.domain.NotAllowedAtUnitException;
import com.orudesk.orudesk.patients.domain.PatientNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class PatientsExceptionHandler {

    @ExceptionHandler(PatientNotFoundException.class)
    ProblemDetail patientNotFound(PatientNotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, "Patient not found", e);
    }

    @ExceptionHandler(NotAllowedAtUnitException.class)
    ProblemDetail notAllowed(NotAllowedAtUnitException e) {
        return problem(HttpStatus.FORBIDDEN, "Not allowed at this unit", e);
    }

    private static ProblemDetail problem(HttpStatus status, String title, RuntimeException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, e.getMessage());
        problem.setTitle(title);
        return problem;
    }
}
