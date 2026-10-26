package com.orudesk.orudesk.patients.domain;

import java.util.Objects;
import java.util.UUID;

public record PatientId(UUID value) {

    public PatientId {
        Objects.requireNonNull(value, "patient id must not be null");
    }

    public static PatientId newId() {
        return new PatientId(UUID.randomUUID());
    }
}
