import { saveError } from '@orudesk/web-core';
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
      throw saveError(response, error, 'The patient was not registered.');
    },
    onSuccess: () =>
      Promise.all([
        queryClient.invalidateQueries({ queryKey: ['patients'] }),
        queryClient.invalidateQueries({ queryKey: ['people'] }),
      ]),
  });
}

/**
 * Saves the front desk's corrections (PUT, Day 49) with both versions read: the patient's and the person's.
 * The core's EditConflictError if either was saved in between, ValidationError for refused fields.
 */
export function useEditPatient(patientId: string) {
  const api = useHealthcareApi();
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (patient: HealthcareSchemas['EditPatientRequest']) => {
      const { data, error, response } = await api.PUT('/api/v1/patients/{patientId}', {
        params: { path: { patientId } },
        body: patient,
      });
      if (data) return data;
      throw saveError(response, error, 'The changes were not saved.');
    },
    onSuccess: (patient) => {
      queryClient.setQueryData(['patients', 'one', patientId], patient);
      return Promise.all([
        queryClient.invalidateQueries({ queryKey: ['patients', 'mrn'] }),
        queryClient.invalidateQueries({ queryKey: ['people'] }),
        queryClient.invalidateQueries({ queryKey: ['audit'] }),
      ]);
    },
  });
}
