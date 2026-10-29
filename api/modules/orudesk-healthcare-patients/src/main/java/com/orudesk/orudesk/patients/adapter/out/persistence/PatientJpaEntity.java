package com.orudesk.orudesk.patients.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.TenantId;

import java.time.LocalDate;
import java.util.UUID;

/** One row of hc_patient. */
@Entity
@Table(name = "hc_patient")
class PatientJpaEntity {

    @Id
    private UUID id;

    @TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "person_id", nullable = false, updatable = false)
    private UUID personId;

    @Column(nullable = false, updatable = false, length = 20)
    private String mrn;

    @Column(name = "registered_at_unit_id", nullable = false, updatable = false)
    private UUID registeredAtUnitId;

    @Column(name = "blood_group", length = 7)
    private String bloodGroup;   // the label, "O+": what the CHECK constraint and a lab report use

    @Column(name = "emergency_contact_name", length = 200)
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", length = 30)
    private String emergencyContactPhone;

    @Column(name = "registered_on", nullable = false)
    private LocalDate registeredOn;

    @Version   // optimistic locking, as in the core's PersonJpaEntity (Day 46); null until first saved
    private Long version;

    protected PatientJpaEntity() {
        // JPA needs a no-argument constructor
    }

    PatientJpaEntity(UUID id, UUID personId, String mrn, UUID registeredAtUnitId, String bloodGroup,
                     String emergencyContactName, String emergencyContactPhone, LocalDate registeredOn) {
        this.id = id;
        this.personId = personId;
        this.mrn = mrn;
        this.registeredAtUnitId = registeredAtUnitId;
        this.bloodGroup = bloodGroup;
        this.emergencyContactName = emergencyContactName;
        this.emergencyContactPhone = emergencyContactPhone;
        this.registeredOn = registeredOn;
    }

    void changeDetails(String bloodGroup, String emergencyContactName, String emergencyContactPhone) {
        this.bloodGroup = bloodGroup;
        this.emergencyContactName = emergencyContactName;
        this.emergencyContactPhone = emergencyContactPhone;
    }

    UUID getId() { return id; }
    UUID getPersonId() { return personId; }
    String getMrn() { return mrn; }
    UUID getRegisteredAtUnitId() { return registeredAtUnitId; }
    String getBloodGroup() { return bloodGroup; }
    String getEmergencyContactName() { return emergencyContactName; }
    String getEmergencyContactPhone() { return emergencyContactPhone; }
    LocalDate getRegisteredOn() { return registeredOn; }
    Long getVersion() { return version; }
}
