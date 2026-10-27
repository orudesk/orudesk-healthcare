package com.orudesk.orudesk.patients.adapter.out.persistence;

import com.orudesk.orudesk.patients.application.port.out.NextMrnPort;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Hands out MRN numbers with one atomic statement against the tenant's counter row.
 *
 * Native SQL, because HQL has no RETURNING. Native SQL skips the tenant filter, so the tenant is passed
 * explicitly: the same tenant Hibernate is using for this session (from the logged-in user's token).
 */
@Component
class MrnCounterAdapter implements NextMrnPort {

    private final EntityManager entityManager;

    MrnCounterAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)   // only ever inside a registration's transaction
    public long next() {
        UUID tenant = (UUID) entityManager.unwrap(Session.class).getTenantIdentifierValue();
        // First patient of a tenant: INSERT the row with 1. Every later one: the row exists, so ON CONFLICT
        // turns the INSERT into an UPDATE of last_value + 1. Either way one statement, and the row stays
        // locked until commit, so a second registration waits here instead of reading the same value.
        Number next = (Number) entityManager.createNativeQuery("""
                        INSERT INTO hc_mrn_counter (tenant_id, last_value) VALUES (:tenant, 1)
                        ON CONFLICT (tenant_id) DO UPDATE SET last_value = hc_mrn_counter.last_value + 1
                        RETURNING last_value
                        """)
                .setParameter("tenant", tenant)
                .getSingleResult();
        return next.longValue();
    }
}
