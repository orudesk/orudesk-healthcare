import type { OrudeskModule } from '@orudesk/web-core';
import { IconStethoscope } from '@tabler/icons-react';

/**
 * The healthcare edition's screens. Receptionists have their role at a hospital or clinic, not tenant-wide, so
 * the entry asks for `rolesAnywhere` (the core's Day 44 addition), matching the API's UnitAccess.
 */
export const healthcareModule: OrudeskModule = {
  id: 'healthcare',
  routes: [
    {
      path: '/patients',
      lazy: async () => ({ Component: (await import('./pages/FindPatientPage')).FindPatientPage }),
    },
    {
      path: '/patients/register',
      lazy: async () => ({
        Component: (await import('./pages/RegisterPatientPage')).RegisterPatientPage,
      }),
    },
    {
      path: '/patients/:patientId/edit',
      lazy: async () => ({ Component: (await import('./pages/EditPatientPage')).EditPatientPage }),
    },
    {
      path: '/patients/:patientId',
      lazy: async () => ({ Component: (await import('./pages/PatientPage')).PatientPage }),
    },
  ],
  navigation: [
    {
      label: 'Patients',
      path: '/patients',
      icon: <IconStethoscope size={18} />,
      rolesAnywhere: ['ADMIN', 'RECEPTIONIST'],
    },
  ],
};
