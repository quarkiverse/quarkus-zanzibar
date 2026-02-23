package io.quarkiverse.zanzibar.authzed.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.build.Jwt;

@QuarkusTest
public class ZanzibarAuthzedResourceTest {

    @Test
    public void testCaveatedPermissionRequiresContext() {
        var jwt = Jwt.subject("caveat-guy").groups("user").audience("everybody").sign();

        given()
                .auth().preemptive().oauth2(jwt)
                .when().get("/authzed/caveat/things/1")
                .then()
                .statusCode(403);

        given()
                .auth().preemptive().oauth2(jwt)
                .when().post("/authzed/caveat/authorize?object=1&relation=caveated_reader&user=caveat-guy")
                .then()
                .statusCode(204);

        given()
                .auth().preemptive().oauth2(jwt)
                .when().get("/authzed/caveat-missing/things/1")
                .then()
                .statusCode(403);

        given()
                .auth().preemptive().oauth2(jwt)
                .when().get("/authzed/caveat/things/1")
                .then()
                .statusCode(200)
                .body(is("Thing 1"));
    }
}
