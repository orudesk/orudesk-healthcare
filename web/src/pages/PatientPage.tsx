import { Alert, Anchor, Card, Group, Loader, SimpleGrid, Stack, Text, Title } from '@mantine/core';
import { formatDate, useMe, usePageTitle } from '@orudesk/web-core';
import { Link, useLocation, useParams } from 'react-router';
import { usePatient } from '../patients';

export function PatientPage() {
  const { patientId = '' } = useParams();
  const justRegistered = (useLocation().state as { justRegistered?: boolean } | null)
    ?.justRegistered;
  const patient = usePatient(patientId);
  const isAdmin = useMe().data?.roles.includes('ADMIN') ?? false; // the audit log is for tenant admins
  const name = patient.data ? `${patient.data.firstName} ${patient.data.lastName}` : 'Patient';
  usePageTitle(patient.data === null ? 'Patient not found' : name);

  return (
    <Stack>
      <Anchor component={Link} to="/patients" size="sm">
        ← Patients
      </Anchor>
      {patient.isPending && <Loader size="sm" aria-label="Loading" />}
      {patient.isError && <Text role="alert">{patient.error.message}</Text>}
      {patient.data === null && <Title order={1}>Patient not found</Title>}
      {patient.data && (
        <>
          {justRegistered && (
            <Alert color="green" role="status" title="Registered" maw={640}>
              Write <strong>{patient.data.mrn}</strong> on the patient&apos;s card: it&apos;s how
              they&apos;ll be found next time.
            </Alert>
          )}
          <Group align="baseline" gap="md">
            <Title order={1}>{name}</Title>
            <Text size="xl">
              <Text span size="sm" c="dimmed">
                MRN{' '}
              </Text>
              <Text span ff="monospace" inherit>
                {patient.data.mrn}
              </Text>
            </Text>
          </Group>
          <Card withBorder maw={640}>
            <SimpleGrid cols={{ base: 1, xs: 2 }} spacing="md" component="dl" m={0}>
              <Field label="Date of birth" value={formatDate(patient.data.dateOfBirth)} />
              <Field label="Blood group" value={patient.data.bloodGroup ?? 'Not known'} />
              <Field label="Phone" value={patient.data.phone ?? '—'} />
              <Field label="Registered on" value={formatDate(patient.data.registeredOn)} />
              <Field
                label="Emergency contact"
                value={
                  patient.data.emergencyContactName
                    ? `${patient.data.emergencyContactName}, ${patient.data.emergencyContactPhone}`
                    : '—'
                }
              />
            </SimpleGrid>
          </Card>
          <Group gap="lg">
            <Anchor component={Link} to={`/patients/${patient.data.id}/edit`} size="sm">
              Edit details
            </Anchor>
            <Anchor component={Link} to={`/people/${patient.data.personId}`} size="sm">
              Their entry in the People registry
            </Anchor>
            {isAdmin && (
              <Anchor component={Link} to={`/activity?subject=${patient.data.id}`} size="sm">
                History
              </Anchor>
            )}
          </Group>
        </>
      )}
    </Stack>
  );
}

function Field({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <Text component="dt" size="sm" c="dimmed">
        {label}
      </Text>
      <Text component="dd" m={0}>
        {value}
      </Text>
    </div>
  );
}
