package com.orudesk.orudesk.patients.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface PatientJpaRepository extends JpaRepository<PatientJpaEntity, UUID> {

    Optional<PatientJpaEntity> findByMrn(String mrn);   // derived query: tenant-filtered like any other
}
