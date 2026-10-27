# Changelog

Every release of Orudesk Healthcare, newest first ([Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
[Semantic Versioning](https://semver.org/spec/v2.0.0.html)). Each release names the core release it is built on.

## [Unreleased]

### Changed

- The edition is called Orudesk Healthcare: repository `orudesk/orudesk-healthcare`, Gradle group
  `com.orudesk.healthcare`, modules `orudesk-healthcare` and `orudesk-healthcare-patients`, image
  `ghcr.io/orudesk/orudesk-healthcare`.

## [1.0.0] - 2026-10-26

Release 1, built on Orudesk core 1.0.0.

### Added

- Patients: register a patient (a person plus blood group and emergency contact) with a gapless medical record number per tenant.
- Docker image `ghcr.io/orudesk/orudesk-healthcare`, and `docker/compose.yaml` to run the edition with its database.
