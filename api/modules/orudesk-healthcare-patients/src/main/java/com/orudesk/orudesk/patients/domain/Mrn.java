package com.orudesk.orudesk.patients.domain;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A medical record number, such as MR0000123: printed on the patient's card and on every report.
 * The database hands out the number (123); the format is a business rule, so it lives here.
 */
public record Mrn(String value) {

    private static final Pattern FORMAT = Pattern.compile("^MR\\d{7,}$");

    public Mrn {
        Objects.requireNonNull(value, "MRN must not be null");
        value = value.trim().toUpperCase(Locale.ROOT);
        if (!FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException("MRN must look like MR0000123");
        }
    }

    /** 123 → MR0000123. Seven digits, then it simply grows: MR10000000 is still valid. */
    public static Mrn fromNumber(long number) {
        if (number < 1) {
            throw new IllegalArgumentException("MRN numbers start at 1");
        }
        return new Mrn("MR%07d".formatted(number));
    }
}
