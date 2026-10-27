import { problemMessage } from '@orudesk/web-core';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useHealthcareApi, type HealthcareSchemas } from './api';

export const BLOOD_GROUPS = ['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'];

/** Units where patients are registered: what the API's UnitAccess expects in X-Org-Unit. */
export const PATIENT_UNIT_TYPES = ['HOSPITAL', 'CLINIC'];

/**
 * The front desk's lookup by the number on the patient's card (GET /api/v1/patients?mrn=). Runs only when
 * asked (`mrn` not empty); null means no such patient, or none this user may see: the API doesn't say which.
 */
export function usePatientByMrn(mrn: string) {
  const api = useHealthcareApi();
  return useQuery({
    queryKey: ['patients', 'mrn', mrn],
    enabled: mrn !== '',
    queryFn: async () => {
      const { data, error, response } = await api.GET('/api/v1/patients', {
        params: { query: { mrn } },
      });
      if (response.status === 404 || response.status === 400) return null;
      if (error) throw new Error('Could not look the patient up');
      return data;
    },
  });
}

export function usePatient(patientId: string) {
  const api = useHealthcareApi();
  return useQuery({
    queryKey: ['patients', 'one', patientId],
    queryFn: async () => {
      const { data, error, response } = await api.GET('/api/v1/patients/{patientId}', {
        params: { path: { patientId } },
      });
      if (response.status === 404 || response.status === 400) return null;
      if (error) throw new Error('Could not load this patient');
      return data;
    },
  });
}

/** The API refused the form: a message per field, as in the core's forms (Day 35). */
export class PatientValidationError extends Error {
  constructor(
    message: string,
    readonly fields: Record<string, string>,
  ) {
    super(message);
  }
}

/** Register at `unitId`, sent explicitly as X-Org-Unit: the unit the form showed (see the school's Day 44). */
export function useRegisterPatient(unitId: string) {
  const api = useHealthcareApi();
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (patient: HealthcareSchemas['RegisterPatientRequest']) => {
      const { data, error, response } = await api.POST('/api/v1/patients', {
        params: { header: { 'X-Org-Unit': unitId } },
        body: patient,
      });
      if (data) return data;
      if (response.status === 400) {
        const fields = (error as { errors?: Record<string, string> } | undefined)?.errors ?? {};
        throw new PatientValidationError(problemMessage(error, 'Some fields are invalid.'), fields);
      }
      throw new Error(problemMessage(error, 'The patient was not registered.'));
    },
    onSuccess: () =>
      Promise.all([
        queryClient.invalidateQueries({ queryKey: ['patients'] }),
        queryClient.invalidateQueries({ queryKey: ['people'] }),
      ]),
  });
}
