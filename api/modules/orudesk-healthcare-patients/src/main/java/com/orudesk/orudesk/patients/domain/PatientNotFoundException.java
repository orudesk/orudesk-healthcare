package com.orudesk.orudesk.patients.domain;

public class PatientNotFoundException extends RuntimeException {

    public PatientNotFoundException(String what) {
        super("No patient with " + what);
    }
}
