package com.orudesk.orudesk;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Patients through the whole hospital app: a hospital and a clinic, a receptionist at the hospital only. */
@SpringBootTest(properties = {
        "orudesk.identity.first-admin.email=test-admin@orudesk.org",
        "orudesk.identity.first-admin.password=admin-password"
})
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class PatientsIntegrationTest {

    private static final String ROOT = "00000000-0000-0000-0000-000000000201";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    private String admin;
    private String receptionist;
    private String hospital;
    private String clinic;

    @BeforeEach
    void aHospitalAClinicAndAReceptionist() throws Exception {
        admin = login("test-admin@orudesk.org", "admin-password");
        hospital = createUnit("test-hospital", "City Hospital", "HOSPITAL");
        clinic = createUnit("test-clinic", "Lake Road Clinic", "CLINIC");

        String userId = JsonPath.read(mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "test-user-desk@orudesk.org", "password": "user-password"}
                                """))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), "$.id");
        mockMvc.perform(post("/api/v1/users/{id}/roles", userId).header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role": "RECEPTIONIST", "orgUnitId": "%s"}
                                """.formatted(hospital)))
                .andExpect(status().is2xxSuccessful());
        receptionist = login("test-user-desk@orudesk.org", "user-password");
    }

    @AfterEach
    void cleanUp() {
        jdbc.update("DELETE FROM hc_patient");
        jdbc.update("DELETE FROM hc_mrn_counter");
        jdbc.update("DELETE FROM party_person WHERE last_name LIKE 'Test-%'");
        jdbc.update("DELETE FROM iam_user WHERE email LIKE 'test-user-%@orudesk.org'");
        jdbc.update("DELETE FROM iam_org_unit WHERE code LIKE 'test-%'");
    }

    @Test
    void theFirstPatientsGetMrnOneTwoThree() throws Exception {
        assertThat(List.of(mrnOf(register(receptionist, hospital, "Asha")), mrnOf(register(receptionist, hospital, "Bina")),
                mrnOf(register(receptionist, hospital, "Chitra"))))
                .containsExactly("MR0000001", "MR0000002", "MR0000003");
    }

    @Test
    void theFrontDeskFindsAPatientByTheMrnOnTheCard() throws Exception {
        String mrn = mrnOf(register(receptionist, hospital, "Asha"));

        mockMvc.perform(get("/api/v1/patients").param("mrn", mrn.toLowerCase())
                        .header("Authorization", "Bearer " + receptionist))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Asha"))
                .andExpect(jsonPath("$.bloodGroup").value("O+"))
                .andExpect(jsonPath("$.emergencyContactName").value("Ravi Test-Kumar"))
                .andExpect(jsonPath("$.registeredAtUnitId").value(hospital));
    }

    /**
     * Twenty registrations at the same moment: twenty different, consecutive MRNs. With "SELECT max + 1"
     * instead of the counter, this fails (see the Day 25 plan).
     */
    @Test
    void concurrentRegistrationsNeverShareAnMrn() throws Exception {
        List<Callable<String>> registrations = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            String name = "Patient" + i;
            registrations.add(() -> mrnOf(register(receptionist, hospital, name)));
        }
        List<String> mrns = new ArrayList<>();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (Future<String> mrn : executor.invokeAll(registrations)) {
                mrns.add(mrn.get());
            }
        }

        assertThat(mrns).doesNotHaveDuplicates().hasSize(20);
        assertThat(mrns.stream().sorted().toList()).startsWith("MR0000001").endsWith("MR0000020");
    }

    @Test
    void aRegistrationIsInTheAuditLogWithWhoDidIt() throws Exception {
        String id = JsonPath.read(register(receptionist, hospital, "Asha")
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), "$.id");

        mockMvc.perform(get("/api/v1/audit-entries").param("subjectId", id).header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].action").value("patients.patient.registered"))
                .andExpect(jsonPath("$.items[0].summary").value("Registered patient MR0000001, Asha Test-Kumar"))
                .andExpect(jsonPath("$.items[0].actorEmail").value("test-user-desk@orudesk.org"));
    }

    @Test
    void aRefusedRegistrationUsesNoNumber() throws Exception {
        register(receptionist, hospital, "Asha");
        register(receptionist, hospital, "Asha-again", "{\"firstName\": \"X\", \"lastName\": \"Test-Kumar\","
                + " \"emergencyContactName\": \"Only A Name\"}")
                .andExpect(status().isBadRequest());

        assertThat(mrnOf(register(receptionist, hospital, "Bina"))).isEqualTo("MR0000002");
    }

    @Test
    void theReceptionistCannotRegisterAtTheClinic() throws Exception {
        register(receptionist, clinic, "Asha")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Not allowed at this unit"));
    }

    @Test
    void butCanLookUpAPatientRegisteredThere() throws Exception {
        String atClinic = JsonPath.read(register(admin, clinic, "Deepa")
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), "$.id");

        mockMvc.perform(get("/api/v1/patients/{id}", atClinic).header("Authorization", "Bearer " + receptionist))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registeredAtUnitId").value(clinic));
    }

    @Test
    void someoneWithoutAPatientsRoleFindsNothing() throws Exception {
        String mrn = mrnOf(register(receptionist, hospital, "Asha"));
        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "test-user-plain@orudesk.org", "password": "user-password"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/patients").param("mrn", mrn)
                        .header("Authorization", "Bearer " + login("test-user-plain@orudesk.org", "user-password")))
                .andExpect(status().isNotFound());
    }

    @Test
    void anUnknownBloodGroupIsRefused() throws Exception {
        register(receptionist, hospital, "Asha", """
                {"firstName": "Asha", "lastName": "Test-Kumar", "bloodGroup": "C+"}
                """)
                .andExpect(status().isBadRequest());
    }

    private ResultActions register(String token, String unit, String firstName) throws Exception {
        return register(token, unit, firstName, """
                {"firstName": "%s", "lastName": "Test-Kumar", "dateOfBirth": "1985-03-14", "phone": "+91 99887 66554",
                 "bloodGroup": "O+", "emergencyContactName": "Ravi Test-Kumar", "emergencyContactPhone": "+91 99887 00000"}
                """.formatted(firstName));
    }

    private ResultActions register(String token, String unit, String firstName, String body) throws Exception {
        return mockMvc.perform(post("/api/v1/patients").header("Authorization", "Bearer " + token)
                .header("X-Org-Unit", unit)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private static String mrnOf(ResultActions result) throws Exception {
        return JsonPath.read(result.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(),
                "$.mrn");
    }

    private String createUnit(String code, String name, String type) throws Exception {
        return JsonPath.read(mockMvc.perform(post("/api/v1/org-units").header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"parentId": "%s", "type": "%s", "code": "%s", "name": "%s"}
                                """.formatted(ROOT, type, code, name)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), "$.id");
    }

    private String login(String email, String password) throws Exception {
        String json = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$.accessToken");
    }
}
