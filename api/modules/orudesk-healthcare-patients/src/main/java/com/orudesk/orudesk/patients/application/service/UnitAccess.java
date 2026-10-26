package com.orudesk.orudesk.patients.application.service;

import com.orudesk.orudesk.identity.OrgUnitAccess;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Who may register patients where: a receptionist or an admin at that unit or above it (Day 19's grants). */
@Component
class UnitAccess {

    private static final String ADMIN = "ADMIN";

    private final OrgUnitAccess access;

    UnitAccess(OrgUnitAccess access) {
        this.access = access;
    }

    boolean mayRegisterAt(UUID unitId) {
        return access.currentUserHasRoleAt(PatientsRoles.RECEPTIONIST, unitId)
                || access.currentUserHasRoleAt(ADMIN, unitId);
    }

    /** May register somewhere in the tenant, so may look patients up (see GetPatientQuery). */
    boolean mayLookUpPatients() {
        return !access.unitsWhereCurrentUserHas(PatientsRoles.RECEPTIONIST).isEmpty()
                || !access.unitsWhereCurrentUserHas(ADMIN).isEmpty();
    }
}
