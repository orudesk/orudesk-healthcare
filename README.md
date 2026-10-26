# Orudesk Healthcare

**The Orudesk edition for hospitals and clinics**: everything in the [Orudesk core](https://github.com/orudesk/orudesk)
(login, roles per unit, organisation units, the people registry) plus what a hospital needs, starting with
patient registration and medical record numbers (MRNs).

> Status: release 1 (1.0.0), built on Orudesk core 1.0.0. Patient data is health data: do not load real
> patients into a development install.

## Quick start: run it

You need only **Docker**, and the core checked out next to this repository (the image is built from both):

```bash
git clone https://github.com/orudesk/orudesk.git
git clone https://github.com/orudesk/orudesk-healthcare.git
cp orudesk/api/.env.example orudesk-healthcare/api/.env       # then set DB_PASSWORD, ORUDESK_JWT_SECRET and the first admin
docker compose --env-file orudesk-healthcare/api/.env -f orudesk-healthcare/docker/compose.yaml up -d --build --wait
```

Swagger UI: <http://localhost:8081/swagger-ui.html>. Log in, then try `registerPatient`.

## Quick start: develop it

You need **JDK 25** and **Docker**, and the core checked out next to this repository:

```bash
git clone https://github.com/orudesk/orudesk.git
git clone https://github.com/orudesk/orudesk-healthcare.git
cd orudesk
cp api/.env.example api/.env                       # then edit the values
docker compose --env-file api/.env -f docker/compose.yaml up -d --wait
cp api/.env ../orudesk-healthcare/api/.env
cd ../orudesk-healthcare/api
./gradlew bootRun
```

Swagger UI: <http://localhost:8080/swagger-ui.html>. Create a clinic (`createOrgUnit`, type `HOSPITAL`), give
someone `RECEPTIONIST` there (`assignRole` with its `orgUnitId`), then `registerPatient` with the
clinic's id in the `X-Org-Unit` header. Each tenant gets gapless MRNs `MR0000001`, `MR0000002`, ...

The web app (Node 22.12+), in a second terminal:

```bash
cd orudesk-healthcare/web
corepack enable
pnpm install         # also links the core's web packages from ../../orudesk/web
pnpm dev             # http://localhost:5173: the core's screens plus Patients
```

`web/` is the healthcare edition's app: `src/main.tsx` starts the core's app with one more module,
`src/healthcareModule.tsx`, which adds Patients: find a patient by the MRN on their card, register one at the
hospital or clinic picked in the switcher, and a patient's page. Its API client is generated from this
edition's contract (`pnpm contract:sync` after changing the API).

## Run the tests

```bash
cd api
./gradlew test       # needs Docker
cd ../web
pnpm check           # format, lint, types, tests
```

## Licence

GNU Affero General Public License v3.0 ([LICENSE](LICENSE)), like the core.
