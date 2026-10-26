package com.orudesk.orudesk.patients.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MrnTest {

    @Test
    void isTheNumberWithSevenDigits() {
        assertThat(Mrn.fromNumber(123).value()).isEqualTo("MR0000123");
    }

    @Test
    void simplyGrowsPastTenMillion() {
        assertThat(Mrn.fromNumber(10_000_000).value()).isEqualTo("MR10000000");
    }

    @Test
    void readsWhatTheFrontDeskTypes() {
        assertThat(new Mrn(" mr0000123 ").value()).isEqualTo("MR0000123");
    }

    @Test
    void refusesAnythingElse() {
        assertThatThrownBy(() -> new Mrn("123")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Mrn.fromNumber(0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void bloodGroupsUseTheLabelsOfALabReport() {
        assertThat(BloodGroup.fromLabel("ab-")).isEqualTo(BloodGroup.AB_NEGATIVE);
        assertThat(BloodGroup.O_POSITIVE.label()).isEqualTo("O+");
    }

    @Test
    void anEmergencyContactNeedsBothParts() {
        assertThatThrownBy(() -> new EmergencyContact("Ravi", " "))
                .hasMessage("an emergency contact needs a name and a phone number");
    }
}
