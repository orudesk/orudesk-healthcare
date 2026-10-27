package com.orudesk.orudesk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * In com.orudesk.orudesk, like the core's own app: Spring scans this package and everything below it, which
 * finds the core's modules (identity, party, shared, in their jars) and this edition's (patients).
 */
@SpringBootApplication
public class OrudeskHealthcareApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrudeskHealthcareApplication.class, args);
    }
}
