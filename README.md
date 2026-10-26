# Orudesk Hospital

**The Orudesk edition for hospitals and clinics**: everything in the [Orudesk core](https://github.com/orudesk/orudesk)
(login, roles per unit, organisation units, the people registry) plus what a hospital needs, starting with
patient registration and medical record numbers (MRNs).

> Status: release 1 (1.0.0), built on Orudesk core 1.0.0. Patient data is health data: do not load real
> patients into a development install.

## Quick start: run it

You need only **Docker**, and the core checked out next to this repository (the image is built from both):

```bash
git clone https://github.com/orudesk/orudesk.git
git clone https://github.com/orudesk/orudesk-hms.git
cp orudesk/api/.env.example orudesk-hms/api/.env       # then set DB_PASSWORD, ORUDESK_JWT_SECRET and the first admin
docker compose --env-file orudesk-hms/api/.env -f orudesk-hms/docker/compose.yaml up -d --build --wait
```

Swagger UI: <http://localhost:8081/swagger-ui.html>. Log in, then try `registerPatient`.

## Quick start: develop it

You need **JDK 25** and **Docker**, and the core checked out next to this repository:

```bash
git clone https://github.com/orudesk/orudesk.git
git clone https://github.com/orudesk/orudesk-hms.git
cd orudesk
cp api/.env.example api/.env                       # then edit the values
docker compose --env-file api/.env -f docker/compose.yaml up -d --wait
cp api/.env ../orudesk-hms/api/.env
cd ../orudesk-hms/api
./gradlew bootRun
```

Swagger UI: <http://localhost:8080/swagger-ui.html>. Create a clinic (`createOrgUnit`, type `HOSPITAL`), give
someone `RECEPTIONIST` there (`assignRole` with its `orgUnitId`), then `registerPatient` with the
clinic's id in the `X-Org-Unit` header. Each tenant gets gapless MRNs `MR0000001`, `MR0000002`, ...

## Run the tests

```bash
cd api
./gradlew test       # needs Docker
```

## Licence

GNU Affero General Public License v3.0 ([LICENSE](LICENSE)), like the core.
