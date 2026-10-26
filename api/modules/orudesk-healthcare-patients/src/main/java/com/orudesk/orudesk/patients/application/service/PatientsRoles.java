package com.orudesk.orudesk.patients.application.service;

import com.orudesk.orudesk.identity.RoleDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class PatientsRoles {

    static final String RECEPTIONIST = "RECEPTIONIST";

    @Bean
    RoleDefinition receptionistRole() {
        return new RoleDefinition(RECEPTIONIST, "Receptionist",
                "Registers patients and looks them up, at the hospitals or clinics where it's given");
    }
}
