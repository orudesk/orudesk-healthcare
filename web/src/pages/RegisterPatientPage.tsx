import { Alert, Button, Group, Loader, Select, Stack, Text, TextInput, Title } from '@mantine/core';
import { useCurrentUnit, useCurrentUnitAccess, usePageTitle } from '@orudesk/web-core';
import type { FormEvent } from 'react';
import { Link, useNavigate } from 'react-router';
import type { HealthcareSchemas } from '../api';
import {
  BLOOD_GROUPS,
  PATIENT_UNIT_TYPES,
  PatientValidationError,
  useRegisterPatient,
} from '../patients';

/** Register a patient at the hospital or clinic picked in the switcher, as admissions do at a school. */
export function RegisterPatientPage() {
  usePageTitle('Register patient');
  const { unitId } = useCurrentUnit();
  const unit = useCurrentUnitAccess(unitId);

  if (unit.isPending) return <Loader size="sm" aria-label="Loading" />;
  const atPatientUnit = PATIENT_UNIT_TYPES.includes(unit.data?.type ?? '');
  const mayRegister =
    atPatientUnit && (unit.data?.roles.some((r) => r === 'ADMIN' || r === 'RECEPTIONIST') ?? false);

  return (
    <Stack maw={560}>
      <Title order={1}>Register patient</Title>
      {unit.data && mayRegister ? (
        <PatientForm unitId={unit.data.id} unitName={unit.data.name} />
      ) : (
        <Alert color="blue" role="status" title="Pick a hospital or clinic">
          {atPatientUnit
            ? `You don't register patients at ${unit.data?.name}. `
            : 'Patients are registered at a hospital or clinic. '}
          Choose one where you work in the switcher at the top.
        </Alert>
      )}
    </Stack>
  );
}

function PatientForm({ unitId, unitName }: { unitId: string; unitName: string }) {
  const register = useRegisterPatient(unitId);
  const navigate = useNavigate();
  const errors = register.error instanceof PatientValidationError ? register.error.fields : {};

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const text = (name: string) => String(form.get(name) ?? '').trim();
    const optional = (name: string) => text(name) || null;
    const patient: HealthcareSchemas['RegisterPatientRequest'] = {
      firstName: text('firstName'),
      lastName: text('lastName'),
      dateOfBirth: optional('dateOfBirth'),
      phone: optional('phone'),
      email: optional('email'),
      bloodGroup: optional('bloodGroup'),
      emergencyContactName: optional('emergencyContactName'),
      emergencyContactPhone: optional('emergencyContactPhone'),
    };
    register.mutate(patient, {
      // The new MRN goes on the card now: the patient's page says so (see `justRegistered`)
      onSuccess: ({ id }) =>
        void navigate(`/patients/${id}`, { replace: true, state: { justRegistered: true } }),
    });
  };

  return (
    <form onSubmit={submit} noValidate>
      <Stack>
        <Text>
          At <strong>{unitName}</strong>
        </Text>
        {/* Field messages go under their fields; a rule across fields (the emergency contact) has none */}
        {register.isError && Object.keys(errors).length === 0 && (
          <Alert color="red" role="alert" title="Not registered">
            {register.error.message}
          </Alert>
        )}
        <Group grow align="flex-start">
          <TextInput name="firstName" label="First name" withAsterisk error={errors.firstName} />
          <TextInput name="lastName" label="Last name" withAsterisk error={errors.lastName} />
        </Group>
        <Group grow align="flex-start">
          <TextInput
            name="dateOfBirth"
            label="Date of birth"
            type="date"
            error={errors.dateOfBirth}
          />
          <Select
            name="bloodGroup"
            label="Blood group"
            data={BLOOD_GROUPS}
            placeholder="Not known"
            clearable
            error={errors.bloodGroup}
          />
        </Group>
        <Group grow align="flex-start">
          <TextInput name="phone" label="Phone" type="tel" error={errors.phone} />
          <TextInput name="email" label="Email" type="email" error={errors.email} />
        </Group>
        <Title order={2} size="h4" mt="sm">
          Emergency contact
        </Title>
        <Text size="sm" c="dimmed">
          Both or neither: a name to ask for, and a number to call.
        </Text>
        <Group grow align="flex-start">
          <TextInput name="emergencyContactName" label="Name" error={errors.emergencyContactName} />
          <TextInput
            name="emergencyContactPhone"
            label="Phone"
            type="tel"
            error={errors.emergencyContactPhone}
          />
        </Group>
        <Group mt="md">
          <Button type="submit" loading={register.isPending}>
            Register
          </Button>
          <Button component={Link} to="/patients" variant="default">
            Cancel
          </Button>
        </Group>
      </Stack>
    </form>
  );
}
