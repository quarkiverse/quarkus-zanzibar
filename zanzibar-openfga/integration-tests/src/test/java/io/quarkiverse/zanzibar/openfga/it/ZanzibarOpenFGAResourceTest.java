package io.quarkiverse.zanzibar.openfga.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.build.Jwt;

@QuarkusTest
public class ZanzibarOpenFGAResourceTest {

    @Test
    public void testHelloEndpoint() {
        var jwt = Jwt.subject("some-guy").audience("everybody").sign();

        given()
                .auth().preemptive().oauth2(jwt)
                .when().get("/openfga/things/1")
                .then()
                .statusCode(403);

        given()
                .auth().preemptive().oauth2(jwt)
                .when().post("/openfga/authorize/some-guy?object=1&relation=reader")
                .then()
                .statusCode(204);

        given()
                .auth().preemptive().oauth2(jwt)
                .when().get("/openfga/things/1")
                .then()
                .statusCode(200)
                .body(is("Thing 1"));
    }
}
