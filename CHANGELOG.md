# Changelog

Every release of Orudesk Healthcare, newest first ([Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
[Semantic Versioning](https://semver.org/spec/v2.0.0.html)). Each release names the core release it is built on.

## [Unreleased]

### Added

- `web/`: the healthcare edition's web app, the core's screens plus Patients: find a patient by MRN, register
  one at the current hospital or clinic (the new MRN shown to write on the card), and a patient's page.
  Receptionists given the role at one unit see it.
- Patient registrations are recorded in the core's audit log; the app includes the core's `orudesk-audit`
  module.
- The edition's OpenAPI contract, saved by `OpenApiContractTest` to `build/openapi/orudesk-healthcare.json`;
  CI checks the web app's copy is up to date.

### Changed

- The contract says 201 Created for `POST /api/v1/patients`, and is sorted and indented.
- The database pool waits at most 5 s for a connection (fail fast with a 503 when the database is down).

- The edition is called Orudesk Healthcare: repository `orudesk/orudesk-healthcare`, Gradle group
  `com.orudesk.healthcare`, modules `orudesk-healthcare` and `orudesk-healthcare-patients`, image
  `ghcr.io/orudesk/orudesk-healthcare`.

## [1.0.0] - 2026-10-26

Release 1, built on Orudesk core 1.0.0.

### Added

- Patients: register a patient (a person plus blood group and emergency contact) with a gapless medical record number per tenant.
- Docker image `ghcr.io/orudesk/orudesk-healthcare`, and `docker/compose.yaml` to run the edition with its database.
