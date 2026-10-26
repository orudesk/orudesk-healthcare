package com.orudesk.orudesk.patients.domain;

import java.util.UUID;

/** The user may not register patients at this hospital or clinic. Becomes a 403. */
public class NotAllowedAtUnitException extends RuntimeException {

    public NotAllowedAtUnitException(UUID unitId) {
        super("You may not register patients at unit " + unitId);
    }
}
