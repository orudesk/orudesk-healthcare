import { coreModule, startOrudeskApp } from '@orudesk/web-core';
import { healthcareModule } from './healthcareModule';

// The healthcare edition: everything the core has, plus patients
startOrudeskApp([coreModule, healthcareModule]);
