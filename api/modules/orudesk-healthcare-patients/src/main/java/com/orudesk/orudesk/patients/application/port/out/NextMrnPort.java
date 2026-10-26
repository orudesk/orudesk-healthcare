package com.orudesk.orudesk.patients.application.port.out;

public interface NextMrnPort {

    /**
     * The next MRN number of the current tenant: 1, 2, 3... Never the same number twice, even under
     * concurrent registrations. Must run inside the registration's transaction: if it rolls back, the
     * number is given back too.
     */
    long next();
}
