package com.orudesk.orudesk.patients.adapter.out.persistence;

import com.orudesk.orudesk.shared.ModuleMigrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** hc_ tables point at party_person and iam_org_unit. No @Order: a vertical's modules run after the core's. */
@Configuration
class PatientsMigrations {

    @Bean
    ModuleMigrations patientsModuleMigrations() {
        return new ModuleMigrations("patients", "classpath:db/migration/patients");
    }
}
