package com.orudesk.orudesk.patients.domain;

import java.util.Arrays;

/** ABO and Rh, as written on a lab report: "O+", "AB-". */
public enum BloodGroup {
    A_POSITIVE("A+"), A_NEGATIVE("A-"), B_POSITIVE("B+"), B_NEGATIVE("B-"),
    AB_POSITIVE("AB+"), AB_NEGATIVE("AB-"), O_POSITIVE("O+"), O_NEGATIVE("O-");

    private final String label;

    BloodGroup(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static BloodGroup fromLabel(String label) {
        return Arrays.stream(values())
                .filter(group -> group.label.equalsIgnoreCase(label.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown blood group: " + label));
    }
}
