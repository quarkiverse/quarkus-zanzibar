package io.quarkiverse.zanzibar.openfga.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import io.quarkiverse.zanzibar.RelationshipManager;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.build.Jwt;

@QuarkusTest
public class ZanzibarOpenFGAResourceTest {

    @Test
    public void testUsingAllAnnotations() {
        var jwt = Jwt.subject("ann-some-guy").audience("everybody").sign();

        given()
                .auth().preemptive().oauth2(jwt)
                .when().get("/openfga/ann/things/1")
                .then()
                .statusCode(403);

        given()
                .auth().preemptive().oauth2(jwt)
                .when().post("/openfga/ann/authorize?object=1&relation=reader")
                .then()
                .statusCode(204);

        given()
                .auth().preemptive().oauth2(jwt)
                .when().get("/openfga/ann/things/1")
                .then()
                .statusCode(200)
                .body(is("Thing 1"));
    }

    @Test
    public void testUsingUserTypeFromJwt() {
        var jwt = Jwt.subject("jwt-some-guy").groups("user").audience("everybody").sign();

        given()
                .auth().preemptive().oauth2(jwt)
                .when().get("/openfga/jwt/things/1")
                .then()
                .statusCode(403);

        given()
                .auth().preemptive().oauth2(jwt)
                .when().post("/openfga/jwt/authorize?object=1&relation=reader")
                .then()
                .statusCode(204);

        given()
                .auth().preemptive().oauth2(jwt)
                .when().get("/openfga/jwt/things/1")
                .then()
                .statusCode(200)
                .body(is("Thing 1"));
    }
}
