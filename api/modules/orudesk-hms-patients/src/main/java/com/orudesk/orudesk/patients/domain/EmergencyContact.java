package com.orudesk.orudesk.patients.domain;

/** Who to call: a name and a phone number, both required. */
public record EmergencyContact(String name, String phone) {

    public EmergencyContact {
        if (name == null || name.isBlank() || phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("an emergency contact needs a name and a phone number");
        }
        name = name.trim();
        phone = phone.trim();
    }
}
