import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { App, buildRoutes, coreModule, createServices } from '@orudesk/web-core';
import { fakeApi, json, tokens } from '@orudesk/web-core/testing';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { healthcareModule } from './healthcareModule';

// A receptionist at City Hospital: the role is a grant there, not tenant-wide
const receptionist = {
  id: 'u1',
  tenantId: 't1',
  email: 'desk@city-hospital.org',
  roles: ['USER'],
  grants: [{ role: 'USER' }, { role: 'RECEPTIONIST', orgUnitId: 'city' }],
  enabled: true,
};
const tree = {
  id: 'group',
  name: 'City Health',
  code: 'CH',
  type: 'GROUP',
  children: [{ id: 'city', name: 'City Hospital', code: 'CITY', type: 'HOSPITAL', children: [] }],
};
const asha = {
  id: 'pt1',
  mrn: 'MR0000042',
  personId: 'p1',
  firstName: 'Asha',
  lastName: 'Kumar',
  dateOfBirth: '1985-03-14',
  phone: '+91 99887 66554',
  bloodGroup: 'O+',
  emergencyContactName: 'Ravi Kumar',
  emergencyContactPhone: '+91 99887 00000',
  registeredAtUnitId: 'city',
  registeredOn: '2026-10-26',
  email: null,
  version: 2,
  personVersion: 5,
};

function openAt(path: string, handlers: Parameters<typeof fakeApi>[0]) {
  window.history.pushState({}, '', path);
  const api = fakeApi({
    'POST /api/v1/auth/refresh': () => tokens(),
    'GET /api/v1/users/me': () => json(receptionist),
    'GET /api/v1/org-units': () => json(tree),
    'GET /api/v1/org-units/current': () =>
      json({
        id: 'city',
        name: 'City Hospital',
        code: 'CITY',
        type: 'HOSPITAL',
        roles: ['RECEPTIONIST'],
      }),
    ...handlers,
  });
  render(<App services={createServices()} modules={[coreModule, healthcareModule]} />);
  return api;
}

describe('the healthcare edition', () => {
  beforeEach(() => localStorage.clear());
  afterEach(() => vi.unstubAllGlobals());

  it("fits next to the core's screens", () => {
    expect(() => buildRoutes([coreModule, healthcareModule])).not.toThrow();
  });

  it('finds a patient by the MRN on their card, in any case', async () => {
    const api = openAt('/patients', {
      'GET /api/v1/patients': (request) =>
        new URL(request.url).searchParams.get('mrn') === 'MR0000042'
          ? json(asha)
          : json({ title: 'Patient not found' }, 404),
      'GET /api/v1/patients/pt1': () => json(asha),
    });
    expect(await screen.findByRole('link', { name: 'Patients' })).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: 'Users' })).not.toBeInTheDocument();

    await userEvent.type(screen.getByLabelText(/MRN/), 'MR0000099{Enter}');
    expect(await screen.findByRole('status')).toHaveTextContent('No patient with MRN MR0000099.');

    await userEvent.clear(screen.getByLabelText(/MRN/));
    await userEvent.type(screen.getByLabelText(/MRN/), 'mr0000042{Enter}');
    expect(await screen.findByRole('heading', { name: 'Asha Kumar' })).toBeInTheDocument();
    expect(screen.getByText('Ravi Kumar, +91 99887 00000')).toBeInTheDocument();
    expect(api.calls('GET /api/v1/patients')).toBe(2);
  });

  it('registers a patient at the current hospital and says to write the MRN on the card', async () => {
    let attempt = 0;
    const api = openAt('/patients/register', {
      'POST /api/v1/patients': () =>
        ++attempt === 1
          ? json(
              {
                title: 'Invalid input',
                detail: 'an emergency contact needs a name and a phone number',
              },
              400,
            )
          : json({ id: 'pt1', mrn: 'MR0000042' }, 201),
      'GET /api/v1/patients/pt1': () => json(asha),
    });

    expect(await screen.findByText('City Hospital', { selector: 'strong' })).toBeInTheDocument();
    await userEvent.type(screen.getByLabelText(/First name/), 'Asha');
    await userEvent.type(screen.getByLabelText(/Last name/), 'Kumar');
    await userEvent.type(screen.getByRole('textbox', { name: 'Name' }), 'Ravi Kumar');
    await userEvent.click(screen.getByRole('button', { name: 'Register' }));
    // A rule across two fields has no field to sit under: it's shown above the form
    expect(await screen.findByRole('alert')).toHaveTextContent(
      'An emergency contact needs a name and a phone number.', // the core's problemMessage tidies it
    );

    const phones = screen.getAllByLabelText('Phone');
    await userEvent.type(phones[1]!, '+91 99887 00000');
    await userEvent.click(screen.getByRole('button', { name: 'Register' }));

    expect(await screen.findByRole('status')).toHaveTextContent(
      "Write MR0000042 on the patient's card",
    );
    const sent = api.requests.filter((r) => r.method === 'POST').at(-1)!;
    expect(sent.headers.get('X-Org-Unit')).toBe('city');
    expect(await sent.json()).toMatchObject({
      firstName: 'Asha',
      bloodGroup: null,
      emergencyContactName: 'Ravi Kumar',
      emergencyContactPhone: '+91 99887 00000',
    });
  });

  it('asks for a hospital or clinic when the current unit is the group', async () => {
    openAt('/patients/register', {
      'GET /api/v1/org-units/current': () =>
        json({ id: 'group', name: 'City Health', code: 'CH', type: 'GROUP', roles: [] }),
    });

    expect(await screen.findByRole('status')).toHaveTextContent(
      'Patients are registered at a hospital or clinic.',
    );
  });

  it('corrects a patient, sending both versions, and shows the saved details', async () => {
    const api = openAt('/patients/pt1/edit', {
      'GET /api/v1/patients/pt1': () => json(asha),
      'PUT /api/v1/patients/pt1': () =>
        json({ ...asha, phone: '+91 90000 22222', version: 2, personVersion: 6 }),
    });

    const phone = (await screen.findAllByLabelText('Phone'))[0]!;
    await userEvent.clear(phone);
    await userEvent.type(phone, '+91 90000 22222');
    await userEvent.click(screen.getByRole('button', { name: 'Save' }));

    expect(await screen.findByText('+91 90000 22222')).toBeInTheDocument();
    expect(window.location.pathname).toBe('/patients/pt1');
    const sent = api.requests.find((r) => r.method === 'PUT')!;
    expect(await sent.json()).toEqual({
      version: 2,
      personVersion: 5,
      firstName: 'Asha',
      lastName: 'Kumar',
      dateOfBirth: '1985-03-14',
      phone: '+91 90000 22222',
      email: null,
      bloodGroup: 'O+',
      emergencyContactName: 'Ravi Kumar',
      emergencyContactPhone: '+91 99887 00000',
    });
  });

  it('offers a reload when someone else saved the patient first', async () => {
    openAt('/patients/pt1/edit', {
      'GET /api/v1/patients/pt1': () => json(asha),
      'PUT /api/v1/patients/pt1': () =>
        json({ title: 'Edit conflict', detail: 'patient pt1 was changed', currentVersion: 3 }, 409),
    });

    await userEvent.click(await screen.findByRole('button', { name: 'Save' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Changed by someone else');
  });
});
