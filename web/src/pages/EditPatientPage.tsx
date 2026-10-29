import {
  Alert,
  Anchor,
  Button,
  Group,
  Loader,
  Select,
  Stack,
  Text,
  TextInput,
  Title,
} from '@mantine/core';
import {
  EditConflictAlert,
  EditConflictError,
  usePageTitle,
  ValidationError,
} from '@orudesk/web-core';
import type { FormEvent } from 'react';
import { Link, useNavigate, useParams } from 'react-router';
import type { HealthcareSchemas } from '../api';
import { BLOOD_GROUPS, useEditPatient, usePatient } from '../patients';

/**
 * The front desk corrects a patient (Day 50): a misspelt name, a new phone, a blood group from the lab. The
 * MRN and where they were registered are facts of the registration, so they're shown, not edited.
 */
export function EditPatientPage() {
  const { patientId = '' } = useParams();
  const patient = usePatient(patientId);
  const edit = useEditPatient(patientId);
  const navigate = useNavigate();
  usePageTitle('Edit patient');
  const errors = edit.error instanceof ValidationError ? edit.error.fields : {};
  const conflict = edit.error instanceof EditConflictError;

  const reload = async () => {
    await patient.refetch();
    edit.reset();
  };

  if (patient.isPending) return <Loader size="sm" aria-label="Loading" />;
  if (patient.isError) return <Text role="alert">{patient.error.message}</Text>;
  if (patient.data === null) return <Title order={1}>Patient not found</Title>;
  const current = patient.data;

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const text = (name: string) => String(form.get(name) ?? '').trim();
    const optional = (name: string) => text(name) || null;
    const changed: HealthcareSchemas['EditPatientRequest'] = {
      version: current.version,
      personVersion: current.personVersion,
      firstName: text('firstName'),
      lastName: text('lastName'),
      dateOfBirth: optional('dateOfBirth'),
      phone: optional('phone'),
      email: optional('email'),
      bloodGroup: optional('bloodGroup'),
      emergencyContactName: optional('emergencyContactName'),
      emergencyContactPhone: optional('emergencyContactPhone'),
    };
    edit.mutate(changed, {
      onSuccess: () => void navigate(`/patients/${patientId}`, { replace: true }),
    });
  };

  return (
    <Stack maw={560}>
      <Anchor component={Link} to={`/patients/${patientId}`} size="sm">
        ← {current.firstName} {current.lastName}
      </Anchor>
      <Title order={1}>Edit patient</Title>
      <Text>
        MRN{' '}
        <Text span ff="monospace" inherit>
          {current.mrn}
        </Text>
      </Text>
      {conflict && (
        <EditConflictAlert onReload={() => void reload()} reloading={patient.isFetching} />
      )}
      {/* Field messages go under their fields; a rule across fields (the emergency contact) has none */}
      {edit.isError && !conflict && Object.keys(errors).length === 0 && (
        <Alert color="red" role="alert" title="Not saved">
          {edit.error.message}
        </Alert>
      )}
      <form key={`${current.version}-${current.personVersion}`} onSubmit={submit} noValidate>
        <Stack>
          <Group grow align="flex-start">
            <TextInput
              name="firstName"
              label="First name"
              withAsterisk
              defaultValue={current.firstName}
              error={errors.firstName}
            />
            <TextInput
              name="lastName"
              label="Last name"
              withAsterisk
              defaultValue={current.lastName}
              error={errors.lastName}
            />
          </Group>
          <Group grow align="flex-start">
            <TextInput
              name="dateOfBirth"
              label="Date of birth"
              type="date"
              defaultValue={current.dateOfBirth ?? ''}
              error={errors.dateOfBirth}
            />
            <Select
              name="bloodGroup"
              label="Blood group"
              data={BLOOD_GROUPS}
              placeholder="Not known"
              clearable
              defaultValue={current.bloodGroup ?? null}
              error={errors.bloodGroup}
            />
          </Group>
          <Group grow align="flex-start">
            <TextInput
              name="phone"
              label="Phone"
              type="tel"
              defaultValue={current.phone ?? ''}
              error={errors.phone}
            />
            <TextInput
              name="email"
              label="Email"
              type="email"
              defaultValue={current.email ?? ''}
              error={errors.email}
            />
          </Group>
          <Title order={2} size="h4" mt="sm">
            Emergency contact
          </Title>
          <Text size="sm" c="dimmed">
            Both or neither: a name to ask for, and a number to call.
          </Text>
          <Group grow align="flex-start">
            <TextInput
              name="emergencyContactName"
              label="Name"
              defaultValue={current.emergencyContactName ?? ''}
              error={errors.emergencyContactName}
            />
            <TextInput
              name="emergencyContactPhone"
              label="Phone"
              type="tel"
              defaultValue={current.emergencyContactPhone ?? ''}
              error={errors.emergencyContactPhone}
            />
          </Group>
          <Group mt="md">
            <Button type="submit" loading={edit.isPending} disabled={conflict}>
              Save
            </Button>
            <Button component={Link} to={`/patients/${patientId}`} variant="default">
              Cancel
            </Button>
          </Group>
        </Stack>
      </form>
    </Stack>
  );
}
