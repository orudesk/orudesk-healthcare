import { useServices } from '@orudesk/web-core';
import createClient, { type Client } from 'openapi-fetch';
import { useMemo } from 'react';
import type { components, paths } from './generated/schema';

/** The healthcare edition's JSON shapes by name: `HealthcareSchemas['PatientDetails']`. */
export type HealthcareSchemas = components['schemas'];

/**
 * A client typed from the healthcare edition's contract (the core's endpoints plus patients), sending through the
 * core's fetch: the access token, the org unit and the outage banner come with it.
 */
export function useHealthcareApi(): Client<paths> {
  const { fetch } = useServices();
  return useMemo(
    () => createClient<paths>({ baseUrl: globalThis.location?.origin ?? '', fetch }),
    [fetch],
  );
}
