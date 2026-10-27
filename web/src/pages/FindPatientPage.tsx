import { Alert, Button, Group, Loader, Stack, Text, TextInput, Title } from '@mantine/core';
import { usePageTitle } from '@orudesk/web-core';
import { IconPlus, IconSearch } from '@tabler/icons-react';
import { useState, type FormEvent } from 'react';
import { Link, Navigate } from 'react-router';
import { usePatientByMrn } from '../patients';

/**
 * The front desk's first screen: type the MRN from the patient's card, or register someone new. No list of all
 * patients on purpose: a receptionist looks up the person in front of them, not browses records.
 */
export function FindPatientPage() {
  usePageTitle('Patients');
  const [mrn, setMrn] = useState('');
  const patient = usePatientByMrn(mrn);

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setMrn(
      String(new FormData(event.currentTarget).get('mrn') ?? '')
        .trim()
        .toUpperCase(),
    );
  };

  if (patient.data) return <Navigate to={`/patients/${patient.data.id}`} />;

  return (
    <Stack maw={520}>
      <Group justify="space-between">
        <Title order={1}>Patients</Title>
        <Button component={Link} to="/patients/register" leftSection={<IconPlus size={16} />}>
          Register patient
        </Button>
      </Group>
      <form onSubmit={submit}>
        <Group align="flex-end">
          <TextInput
            name="mrn"
            label="MRN"
            description="The medical record number on the patient's card, such as MR0000042"
            leftSection={<IconSearch size={16} />}
            autoFocus
            style={{ flex: 1 }}
          />
          <Button type="submit" variant="default">
            Find
          </Button>
        </Group>
      </form>
      {patient.isFetching && <Loader size="sm" aria-label="Loading" />}
      {patient.isError && <Text role="alert">{patient.error.message}</Text>}
      {patient.data === null && (
        <Alert color="yellow" role="status">
          No patient with MRN {mrn}. Check the number on the card, or register them.
        </Alert>
      )}
    </Stack>
  );
}
