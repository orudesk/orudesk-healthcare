package com.orudesk.orudesk.patients.application.port.in;

import java.util.UUID;

/** What the front desk needs straight away: the id, and the MRN to print on the card. */
public record PatientRegistered(UUID id, String mrn) {
}
