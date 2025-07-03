package org.example.multi_tenant_app.web.controllers;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.example.multi_tenant_app.data.entities.Tenant; // Adjust if Tenant DTO is used for POST
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // To run tests in a specific order if needed (e.g. POST then GET)
public class TenantResourceTest {

    private static final String TENANTS_ENDPOINT = "/api/v1/tenants";
    private static String createdTenantId; // Store ID from POST to use in GET/PUT/DELETE

    @Test
    @Order(1)
    @TestSecurity(user = "testUser", roles = {"user"}) // Provide a mock authenticated user
    public void testCreateTenant() {
        Tenant newTenant = new Tenant("Test Tenant Alpha", "ACTIVE");
        // Note: Tenant entity directly used. In a real app, a TenantCreationDTO might be preferred.

        String responseString = given()
                .contentType(ContentType.JSON)
                .body(newTenant)
                .when()
                .post(TENANTS_ENDPOINT)
                .then()
                .statusCode(201) // CREATED
                .body("id", notNullValue())
                .body("name", equalTo("Test Tenant Alpha"))
                .body("status", equalTo("ACTIVE"))
                .extract().asString();

        // Extract the ID for subsequent tests
        createdTenantId = io.restassured.path.json.JsonPath.from(responseString).getString("id");
        System.out.println("Created Tenant ID: " + createdTenantId);
    }

    @Test
    @Order(2)
    @TestSecurity(user = "testUser", roles = {"user"})
    public void testGetTenantById_Found() {
        given()
                .when()
                .get(TENANTS_ENDPOINT + "/" + createdTenantId)
                .then()
                .statusCode(200) // OK
                .body("id", equalTo(createdTenantId))
                .body("name", notNullValue()) // Name might be different if updated, but should exist
                .body("status", notNullValue());
    }

    @Test
    @Order(3)
    @TestSecurity(user = "testUser", roles = {"user"})
    public void testGetTenantById_NotFound() {
        String randomUuid = UUID.randomUUID().toString();
        given()
                .when()
                .get(TENANTS_ENDPOINT + "/" + randomUuid)
                .then()
                .statusCode(404); // NOT_FOUND (Assuming the placeholder returns 404)
                                  // This will depend on the actual implementation of getTenantById
                                  // For now, TenantResource placeholder returns a dummy tenant or 404.
                                  // The actual behavior might be different with a real service.
                                  // Current placeholder always returns a dummy tenant, so this might need adjustment
                                  // once the resource is properly implemented.
                                  // For now, let's assume a basic 404 is possible if not found.
                                  // The current placeholder for getTenantById returns a dummy tenant, so this test would fail.
                                  // Let's change the test to reflect the placeholder's behavior (always finds a dummy).
                                  // Better: let's assume the placeholder for getTenantById is updated to return 404.
                                  // The placeholder in TenantResource currently does:
                                  // Tenant tenant = new Tenant("Dummy Tenant " + id.toString(), "ACTIVE"); tenant.id = id; return Response.ok(tenant).build();
                                  // This means it will always find a tenant.
                                  // For a "not found" test to pass with the current placeholder, we'd need to change the placeholder
                                  // or accept this test won't reflect a true "not found" scenario yet.
                                  // Let's proceed assuming the placeholder is what it is, and this test will check that behavior.
                                  // Actually, the placeholder has an if (tenant != null) check, but it's always true.
                                  // Let's keep the 404 expectation, as it's what a real service *should* do.
    }


    @Test
    @Order(4)
    @TestSecurity(user = "testUser", roles = {"user"})
    public void testGetAllTenants() {
        given()
                .when()
                .get(TENANTS_ENDPOINT)
                .then()
                .statusCode(200) // OK
                .body("size()", greaterThanOrEqualTo(1)) // Expecting at least the one we created (or more if others exist)
                .body("[0].id", notNullValue())
                .body("[0].name", notNullValue());
    }

    @Test
    @Order(5)
    @TestSecurity(user = "testUser", roles = {"user"})
    public void testUpdateTenant() {
        Tenant updatedTenant = new Tenant("Test Tenant Alpha Updated", "INACTIVE");
        updatedTenant.id = UUID.fromString(createdTenantId); // Ensure ID is set for the update

        given()
                .contentType(ContentType.JSON)
                .body(updatedTenant)
                .when()
                .put(TENANTS_ENDPOINT + "/" + createdTenantId)
                .then()
                .statusCode(200) // OK
                .body("id", equalTo(createdTenantId))
                .body("name", equalTo("Test Tenant Alpha Updated"))
                .body("status", equalTo("INACTIVE"));
    }

    @Test
    @Order(6)
    @TestSecurity(user = "testUser", roles = {"user"})
    public void testDeleteTenant() {
        given()
                .when()
                .delete(TENANTS_ENDPOINT + "/" + createdTenantId)
                .then()
                .statusCode(204); // NO_CONTENT

        // Optionally, verify it's gone with a GET
        given()
                .when()
                .get(TENANTS_ENDPOINT + "/" + createdTenantId)
                .then()
                .statusCode(404); // Assuming the placeholder for GET will now effectively be a 404
                                  // This depends on the actual delete implementation making it not findable.
                                  // The placeholder GET will still return a dummy. This part of the test
                                  // will only be fully valid once GET and DELETE are properly implemented.
    }

    @Test
    @Order(7)
    // Test unauthenticated access
    public void testGetTenantById_Unauthenticated() {
        given()
                .when()
                .get(TENANTS_ENDPOINT + "/" + UUID.randomUUID().toString())
                .then()
                .statusCode(401); // UNAUTHORIZED
    }
}
